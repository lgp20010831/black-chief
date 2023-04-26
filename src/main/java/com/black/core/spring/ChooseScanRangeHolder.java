package com.black.core.spring;

import com.black.core.builder.Col;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChooseScanRangeHolder implements ProcessorScanRangeHolder{


    private static final Collection<String> ranges = new ArrayList<>();

    @SuppressWarnings("ALL")
    private static final String regEx = "[ `~!@#$%^&*()+=|{}‘:;‘,\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";

    public static String[] obtainRanges(){
        return ranges.toArray(new String[0]);
    }

    public static void reset(){
        ranges.clear();
    }

    @Override
    public void screenRange(String[] allRange) {
        Collection<String> joinSource = new ArrayList<>();
        int length = allRange.length;
        if (length > 1){
            top :for (int i = 0; i < allRange.length; i++) {
                String a1 = allRange[i];
                if (isVaildRange(a1)){
                    continue top;
                }
                for (String s : allRange) {
                    if (s.equals(a1)){
                        continue;
                    }
                    String scanningRange = withinScanningRange(a1, s);
                    if (s.equals(scanningRange)){
                        continue top;
                    }
                }
                joinSource.add(a1);
            }
        }else {
            joinSource.addAll(Arrays.asList(allRange));
        }

        Iterator<String> iterator = ranges.iterator();
        while (iterator.hasNext()) {
            String fr = iterator.next();
            Iterator<String> stringIterator = joinSource.iterator();
            while (stringIterator.hasNext()) {
                String ar = stringIterator.next();
                if (isVaildRange(ar)){
                    continue;
                }
                String result = withinScanningRange(fr, ar);
                if (result == null){
                    continue;
                }
                if ( result.equals(ar)){
                    iterator.remove();
                }else {
                    stringIterator.remove();
                }
            }
        }
        ranges.addAll(joinSource);
    }

    public static void filterVaildRange(Collection<String> target, String[] sources){
        Collection<String> joinSource = new ArrayList<>();
        int length = sources.length;
        if (length > 1){
            top :for (int i = 0; i < sources.length; i++) {
                String a1 = sources[i];
                if (isVaildRange(a1)){
                    continue top;
                }
                for (String s : sources) {
                    if (s.equals(a1)){
                        continue;
                    }
                    String scanningRange = withinScanningRange(a1, s);
                    if (s.equals(scanningRange)){
                        continue top;
                    }
                }
                joinSource.add(a1);
            }
        }else {
            joinSource.addAll(Arrays.asList(sources));
        }

        Iterator<String> iterator = target.iterator();
        while (iterator.hasNext()) {
            String fr = iterator.next();
            Iterator<String> stringIterator = joinSource.iterator();
            while (stringIterator.hasNext()) {
                String ar = stringIterator.next();
                if (isVaildRange(ar)){
                    continue;
                }
                String result = withinScanningRange(fr, ar);
                if (result == null){
                    continue;
                }
                if ( result.equals(ar)){
                    iterator.remove();
                }else {
                    stringIterator.remove();
                }
            }
        }
        target.addAll(joinSource);
    }

    public static String withinScanningRange(String target, String value){

        String[] targets = target.split("\\.");
        String[] values = value.split("\\.");
        int loop = Math.min(targets.length, values.length);
        for (int i = 0; i < loop; i++) {
            String t = targets[i];
            String v = values[i];
            if (!t.equals(v)){
                return null;
            }
        }
        return targets.length > values.length ? value : target;
    }

    public static boolean isVaildRange(String range){

        if (range == null || "".equals(range)){
            return true;
        }
        String[] characterAfterSegmentations = range.split("\\.");
        Pattern p = Pattern.compile(regEx);
        for (String segmentation : characterAfterSegmentations) {
            Matcher m = p.matcher(segmentation);
            if (m.find()) {
                return true;
            }
        }
        return false;
    }


    public static void main(String[] args) {
        ProcessorScanRangeHolder processorScanRangeHolder = new ChooseScanRangeHolder();
        String s1 ="com.example.springautothymeleaf";
        String s2 = "com.example.springautothymeleaf.aop";
        String r = "org.sd.ww";
        String v ="sreg$$%.@Ss";
        String e ="com.example.springautothymeleaf.spring.util";
        processorScanRangeHolder.screenRange(Col.ar(s1, s2, r, v ,e));
        System.out.println(ChooseScanRangeHolder.obtainRanges().length);
    }
}
