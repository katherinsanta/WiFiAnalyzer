/*
 * WiFiAnalyzer
 * Copyright (C) 2017  VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.vrem.wifianalyzer.wifi.timegraph;

import android.support.annotation.NonNull;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.vrem.wifianalyzer.wifi.graphutils.GraphConstants;
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewWrapper;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class DataManager implements GraphConstants {
    private int scanCount;
    private int xValue;
    private TimeGraphCache timeGraphCache;

    DataManager() {
        this.scanCount = 0;
        this.xValue = 0;
        this.timeGraphCache = new TimeGraphCache();
    }

    Set<WiFiDetail> addSeriesData(@NonNull GraphViewWrapper graphViewWrapper, @NonNull List<WiFiDetail> wiFiDetails, int levelMax) {
        Set<WiFiDetail> inOrder = new TreeSet<>(wiFiDetails);
        for (WiFiDetail wiFiDetail : inOrder) {
            addData(graphViewWrapper, wiFiDetail, levelMax);
        }
        adjustData(graphViewWrapper, inOrder);
        Set<WiFiDetail> result = getNewSeries(inOrder);
        xValue++;
        if (scanCount < MAX_SCAN_COUNT) {
            scanCount++;
        }
        if (scanCount == 2) {
            graphViewWrapper.setHorizontalLabelsVisible(true);
        }
        return result;
    }

    void adjustData(@NonNull GraphViewWrapper graphViewWrapper, @NonNull Set<WiFiDetail> wiFiDetails) {
        for (WiFiDetail wiFiDetail : graphViewWrapper.differenceSeries(wiFiDetails)) {
            DataPoint dataPoint = new DataPoint(xValue, MIN_Y + MIN_Y_OFFSET);
            boolean drawBackground = wiFiDetail.getWiFiAdditional().getWiFiConnection().isConnected();
            graphViewWrapper.appendToSeries(wiFiDetail, dataPoint, scanCount, drawBackground);
            timeGraphCache.add(wiFiDetail);
        }
        timeGraphCache.clear();
    }

    Set<WiFiDetail> getNewSeries(@NonNull Set<WiFiDetail> wiFiDetails) {
        Set<WiFiDetail> results = new HashSet<>(wiFiDetails);
        results.addAll(timeGraphCache.active());
        return results;
    }

    void addData(@NonNull GraphViewWrapper graphViewWrapper, @NonNull WiFiDetail wiFiDetail, int levelMax) {
        boolean drawBackground = wiFiDetail.getWiFiAdditional().getWiFiConnection().isConnected();
        int level = Math.min(wiFiDetail.getWiFiSignal().getLevel(), levelMax);
        if (graphViewWrapper.isNewSeries(wiFiDetail)) {
            DataPoint dataPoint = new DataPoint(xValue, scanCount > 0 ? MIN_Y + MIN_Y_OFFSET : level);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{dataPoint});
            graphViewWrapper.addSeries(wiFiDetail, series, drawBackground);
        } else {
            DataPoint dataPoint = new DataPoint(xValue, level);
            graphViewWrapper.appendToSeries(wiFiDetail, dataPoint, scanCount, drawBackground);
        }
        timeGraphCache.reset(wiFiDetail);
    }

    void setTimeGraphCache(@NonNull TimeGraphCache timeGraphCache) {
        this.timeGraphCache = timeGraphCache;
    }

    int getXValue() {
        return xValue;
    }

    int getScanCount() {
        return scanCount;
    }

    void setScanCount(int scanCount) {
        this.scanCount = scanCount;
    }
}
