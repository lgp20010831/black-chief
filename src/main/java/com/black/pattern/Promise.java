package com.black.pattern;

import com.black.function.Consumer;
import com.black.function.Function;
import com.black.function.Supplier;

import java.util.LinkedList;

@SuppressWarnings("all")
public class Promise<T> {

    private final Supplier<T> supplier;

    private final LinkedList<Node> nodeLinkedList = new LinkedList<>();

    public Promise(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Promise<T> fetch(Supplier<T> supplier){
        return new Promise<>(supplier);
    }

    public <R> Promise fun(Function<T, R> function){
        nodeLinkedList.addLast(new FunNode<>(function));
        return this;
    }

    public Promise con(Consumer<T> consumer){
        nodeLinkedList.addLast(new ConsumerNode<>(consumer));
        return this;
    }

    public <R> Promise sup(Supplier<R> supplier){
        nodeLinkedList.addLast(new SupplierNode<>(supplier));
        return this;
    }

    public <R> Promise error(Function<Throwable, R> function){
        nodeLinkedList.addLast(new ErrorFunNode<>(function));
        return this;
    }

    public void run(){
        call();
    }

    public <R> R call(){
        Node node;
        Throwable ex = null;
        T param = null;
        try {
             param = supplier.get();
        } catch (Throwable e) {
            ex = e;
        }
        R result = null;
        while ((node = nodeLinkedList.poll()) != null){
            if (ex != null){
                if (!node.isThowable()){
                    throw new IllegalStateException(ex);
                }

                try {
                    if (node instanceof ErrorFunNode){
                        result = ((ErrorFunNode<R>) node).invoke(ex);
                    }else if (node instanceof ErrorConsumerNode){
                        ((ErrorConsumerNode) node).invoke(ex);
                    }
                }catch (Throwable e){
                    ex = e;
                }
                continue;
            }

            try {
                if (node instanceof FunNode){
                    result = ((FunNode<T, R>) node).invoke(param);
                }else if (node instanceof ConsumerNode){
                    ((ConsumerNode<T>) node).invoke(param);
                }else if (node instanceof SupplierNode){
                    result = ((SupplierNode<R>) node).invoke();
                }

            }catch (Throwable e){
                ex = e;
            }

        }
        return result;
    }

    static class Node{

        protected boolean thowable = false;

        public boolean isThowable() {
            return thowable;
        }
    }

    static class ErrorFunNode<R> extends Node{

        protected final Function<Throwable, R> function;
        ErrorFunNode(Function<Throwable, R> function) {
            this.function = function;
            thowable = true;
        }

        public R invoke(Throwable ex) throws Throwable {
            return function.apply(ex);
        }

    }

    static class ErrorConsumerNode extends Node{
        protected final Consumer<Throwable> consumer;

        ErrorConsumerNode(Consumer<Throwable> consumer) {
            this.consumer = consumer;
        }

        public void invoke(Throwable ex) throws Throwable {
            consumer.accept(ex);
        }
    }

    static class FunNode<T, R> extends Node{

        protected final Function<T, R> function;

        FunNode(Function<T, R> function) {
            this.function = function;
        }

        public R invoke(T t) throws Throwable {
            return function.apply(t);
        }
    }

    static class ConsumerNode<T> extends Node{

        protected final Consumer<T> consumer;

        ConsumerNode(Consumer<T> consumer) {
            this.consumer = consumer;
        }

        public void invoke(T t) throws Throwable {
            consumer.accept(t);
        }
    }

    static class SupplierNode<R> extends Node{

        protected final Supplier<R> supplier;

        SupplierNode(Supplier<R> supplier) {
            this.supplier = supplier;
        }

        public R invoke() throws Throwable {
            return supplier.get();
        }
    }
}
