/*
 * Copyright 2020 Sharath Chandrashekhara
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.buffalo.sc296.kegtron;

public class KegStats {
    public static float ML_TO_GALLON = 3785.41F;

    int dispensed;
    int size;
    int tapNumber;
    String beerName;
    int initVol;

    public KegStats() {

    }

    public int getDispensed() {
        return dispensed;
    }

    public KegStats(int tapNumber) {
        this.tapNumber = tapNumber;
    }

    public void setDispensed(int dispensed) {
        this.dispensed = dispensed;
    }

    public int getRemaining() {
        return initVol - dispensed;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTapNumber() {
        return tapNumber;
    }

    public void setTapNumber(int tapNumber) {
        this.tapNumber = tapNumber;
    }

    public String getBeerName() {
        return beerName;
    }

    public void setBeerName(String beerName) {
        this.beerName = beerName;
    }

    public void setInitVol(int vol) {
        this.initVol = vol;
    }

    public String toString() {
        return "{Tap Number: " + tapNumber + ", size: " + size + ", dispensed: " + dispensed +
                ", remaining: " + getRemaining() +
                ", beerName: " + beerName + ", initialVolume: " + initVol + "}";
    }
}
