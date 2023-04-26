package com.black.core.chain;

import java.util.*;

public class Group<G>  {

    List<G> source;

    public Group(){
        source = new ArrayList<>();
    }

    public Group(List<G> source){
        this.source = source;
    }

    public Collection<G> getSource() {
        return source;
    }

    public boolean hasMore(){
        return !source.isEmpty();
    }

    public G getSingle(){
        if(hasMore()){
            return source.get(0);
        }
        return null;
    }
}
