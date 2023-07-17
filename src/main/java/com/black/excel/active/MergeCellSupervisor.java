package com.black.excel.active;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author 李桂鹏
 * @create 2023-07-13 16:42
 */
@SuppressWarnings("all") @Getter @Setter
public class MergeCellSupervisor {

    private Sheet sheet;

    private Set<String> supervises;

    private Map<String, SuperviseInfo> superviseInfoMap;

    private boolean autoProcessCell = false;

    private Consumer<CellStyle> configCellStyle;

    public MergeCellSupervisor(Sheet sheet){
        this.sheet = sheet;
    }

    public void setSupervises(Set<String> supervises) {
        this.supervises = supervises;
        superviseInfoMap = new LinkedHashMap<>();
    }

    public void removeInMergeCell(int rowNum, int colNum){
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        Iterator<CellRangeAddress> iterator = mergedRegions.iterator();
        while (iterator.hasNext()) {
            CellRangeAddress address = iterator.next();
            if (address.isInRange(rowNum, colNum)){
                iterator.remove();
                break;
            }
        }
    }

    public void removeInMergeCell(Cell cell){
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        Iterator<CellRangeAddress> iterator = mergedRegions.iterator();
        while (iterator.hasNext()) {
            CellRangeAddress address = iterator.next();
            if (address.isInRange(cell)){
                iterator.remove();
                break;
            }
        }
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
        if (isAutoProcessCell() && configCellStyle != null){
            //if sr = 5 er = 5 sc = 0 ec = 1
            for (int r = sr; r <= er; r++) {
                for (int c = sc; c <= ec; c++) {
                    Row row = sheet.getRow(r);
                    if (row == null){
                        row = sheet.createRow(r);
                    }
                    Cell cell = row.getCell(c);
                    if (cell == null){
                        cell = row.createCell(c);
                    }
                    CellStyle cellStyle = cell.getCellStyle();
                    configCellStyle.accept(cellStyle);
                }
            }
        }
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
