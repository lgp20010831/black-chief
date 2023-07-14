package com.black.excel.active;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.*;

/**
 * @author 李桂鹏
 * @create 2023-07-13 16:42
 */
@SuppressWarnings("all")
public class MergeCellSupervisor {

    private Sheet sheet;

    private Set<String> supervises;

    private Map<String, SuperviseInfo> superviseInfoMap;

    public MergeCellSupervisor(Sheet sheet){
        this.sheet = sheet;
    }

    public void setSupervises(Set<String> supervises) {
        this.supervises = supervises;
        superviseInfoMap = new LinkedHashMap<>();
    }

    public boolean isMerged(Cell cell){
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        for (CellRangeAddress region : mergedRegions) {
            if (region.isInRange(cell)){
                return true;
            }
        }
        return false;
    }

    public boolean isMerged(int row, int col){
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        for (CellRangeAddress mergedRegion : mergedRegions) {
            if (mergedRegion.isInRange(row, col)) {
                return true;
            }
        }
        return false;
    }

    public void merge(int sr, int er, int sc, int ec){
        if (sr == er && sc == ec){
            return;
        }
        if (isMerged(sr, sc)){
            return;
        }
        sheet.addMergedRegion(new CellRangeAddress(sr, er, sc, ec));
    }

    public boolean mergeBySupervise(int sr, int er, int sc, int ec, String title, Object currentValue){
        if (supervises == null || !supervises.contains(title)){
            merge(sr, er, sc, ec);
            return true;
        }

        return supervise(sr, er, sc, ec, title, currentValue);
    }

    public boolean supervise(int sr, int er, int sc, int ec, String title, Object currentValue){
        if (supervises == null || !supervises.contains(title)){
            return true;
        }
        SuperviseInfo superviseInfo = superviseInfoMap.computeIfAbsent(title, k -> new SuperviseInfo(currentValue));
        if (superviseInfo.match(currentValue)) {
            superviseInfo.addHeight( er - sr + 1);
            return false;
        }else {
            int ehr = sr + superviseInfo.getHeight() - 1;
            merge(sr, ehr, sc, ec);
            superviseInfo.reset();
            return true;
        }
    }

    static class SuperviseInfo{

        private int height = 0;

        private Object pop;

        public SuperviseInfo(Object pop){
            this.pop = pop;
        }

        public boolean match(Object val){
            boolean equals = Objects.equals(pop, val);
            pop = val;
            return equals;
        }

        public void addHeight(){
            addHeight(1);
        }

        public void addHeight(int i){
            height = height + i;
        }

        public int getHeight() {
            return height;
        }

        public void reset(){
            height = 1;
        }
    }
}
