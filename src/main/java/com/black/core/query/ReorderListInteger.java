package com.black.core.query;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("all")
public class ReorderListInteger {

    /**
     * 最大的排序数，数据量一旦超过最大值
     * 性能会非常差
     */
    private final int MAX_SORT_NUM=5000;

    /**
     * 升序
     * @param targetList 对目标集合进行升序
     * @return 返回新的集合
     */
    public <V> List<V> ascendingOrder(List<V> targetList)       { return sortOrder(targetList, true); }

    /**
     * 降序
     * @param list 对目标集合进行降序
     * @return 返回新的集合
     */
    public <V> List<V> descendingOrder(List<V> list)            { return sortOrder(list, false); }

    /**
     * 排序
     * @param list 目标集合
     * @param ascending 是否为升序
     * @return 返回新的排序好的集合
     */
    @SuppressWarnings("all")
    private synchronized <V> List<V> sortOrder(List<V> list,boolean ascending){

        //不允许为null，或是size=0
        assert list != null && list.size() > 0 : "list is null or size = 0" ;
        if (list.size() >= MAX_SORT_NUM)    /* 抛出性能异常*/
            throw new MaxSortPerformanceException();

        //取第一个元素，判断其类型
        if (list.get(0) instanceof Integer)
            return integerSort(list,ascending);
        //直接将集合排序
        Object[] beforeSort = list.toArray();

        //排序结束后的list集合
        Object[] afterSort = new Object[list.size()];
        Field field = null;
        String fn = null;
        Object o;
        /*
             将集合转成数组，遍历得出第一个元素，拿到元素该被排序的字段，假设是个int类型
             先判断   afterSort 里有没有数据，如果没有，就把这个值存进去，然后continue
             该遍历 afterSort 倒着遍历，先拿到最后一个值，按照升序的话，就是最大的值
             拿到afterSort里的每个值得该被排序的字段，判断当前beforeSort
             的这个字段值，是否大于  afterSort 的这个字段，如果false（小于），就让这个字段
         */
        for (int i = 0; i < beforeSort.length ; i++) {
            Object v= beforeSort[i];

            //赋值第一个数据
            if (afterSort[0] == null){
                afterSort[0] = v;
                continue;
            }
            Field[] fields = v.getClass().getDeclaredFields();
            if (field == null || fn == null){
                for (Field f :fields){
                    f.setAccessible(true);
                    try {
                        if (f.isAnnotationPresent(SortField.class)
                                && f.get(v) instanceof Integer
                                && (field = f) != null
                                && (fn = field.getName()) != null)
                            break;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            try {
                //拿到比较字段
                Integer a=(Integer) field.getInt(v);

                // v → a ;  va → ai 关联
                // 1.2.3.7.8 插入5
                for (int j = i-1; j >=0; j--) {
                    Object va = afterSort[j];
                    Field afterIf=va.getClass().getDeclaredField(fn);
                    afterIf.setAccessible(true);
                    Integer ai;
                    //是升序，且原本的值大于插入的值，需要交换位置
                    if (ascending && (ai = afterIf.getInt(va)) > a
                            ||  //是降序，原本的值小于插入的值
                            !ascending && (ai = afterIf.getInt(va)) < a) {
                        afterSort[j] = v;
                        afterSort[j + 1] = va;
                    }else{
                        afterSort[j + 1] = v;
                        break;
                    }
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return (List<V>) Arrays.asList(afterSort);
    }

    /**
     * 针对 泛型为integer类型的集合进行升降序排列
     * @param intList 目标集合
     * @param ascending 升序降序？
     * @return 返回排序完成的集合
     */
    @SuppressWarnings("all")
    private <V> List<V> integerSort(List<V> intList,boolean ascending){
        Integer[] ints=new Integer[intList.size()];

        //遍历intList
        for (int i = 0; i <intList.size() ; i++) {
            //赋初始值
            if (i==0)   {
                ints[0] = (Integer) intList.get(0);
                continue;
            }
            Integer var1 = (Integer) intList.get(i);
            for (int j = i - 1; j >= 0 ; --j) {
                Integer var2 = ints[j];

                //是升序，且原本的值大于插入的值，需要交换位置
                if ((ascending && var2 > var1)
                        ||  //是降序，原本的值小于插入的值
                        (!ascending && var2<var1)) {
                    ints[j] = var1;
                    ints[j + 1] = var2;
                }else{
                    ints[j + 1] = var1;
                    break;
                }
            }
        }
        return (List<V>) Arrays.asList(ints);
    }

    /**  排序集合量过大引起的性能问题 */
    public static class MaxSortPerformanceException extends RuntimeException{}
}
