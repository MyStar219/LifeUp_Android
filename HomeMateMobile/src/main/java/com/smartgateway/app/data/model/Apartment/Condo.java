package com.smartgateway.app.data.model.Apartment;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Terry on 28/6/16.
 */
public class Condo {

    /**
     * condo_id : 1
     * buildings : [{"building_id":1,"levels":[{"level":1,"units":[{"unit_id":1,"unit_no":432},{"unit_id":1,"unit_no":433}]}]}]
     */

    private CondoBean Condo;

    public CondoBean getCondo() {
        return Condo;
    }

    public void setCondo(CondoBean Condo) {
        this.Condo = Condo;
    }

    public static class CondoBean {
        private String condo_id;
        /**
         * building_id : 1
         * levels : [{"level":1,"units":[{"unit_id":1,"unit_no":432},{"unit_id":1,"unit_no":433}]}]
         */

        private List<BuildingsBean> buildings;


        public String getCondo_id() {
            return condo_id;
        }

        public void setCondo_id(String condo_id) {
            this.condo_id = condo_id;
        }

        public List<BuildingsBean> getBuildings() {
            return buildings;
        }

        public void setBuildings(List<BuildingsBean> buildings) {
            this.buildings = buildings;
        }

        public static class BuildingsBean {
            private int building_id;
            private String block;
            /**
             * level : 1
             * units : [{"unit_id":1,"unit_no":432},{"unit_id":1,"unit_no":433}]
             */

            private List<LevelsBean> levels;

            public int getBuilding_id() {
                return building_id;
            }

            public void setBuilding_id(int building_id) {
                this.building_id = building_id;
            }

            public void setBlock(String block) {
                this.block = block;
            }
            public String getBlock() {
                return this.block;
            }
            public List<LevelsBean> getLevels() {
                return levels;
            }

            public void setLevels(List<LevelsBean> levels) {
                this.levels = levels;
            }

            public static class LevelsBean {
                private int level;
                /**
                 * unit_id : 1
                 * unit_no : 432
                 */

                private List<UnitsBean> units;

                public int getLevel() {
                    return level;
                }

                public void setLevel(int level) {
                    this.level = level;
                }

                public List<UnitsBean> getUnits() {
                    return units;
                }

                public void setUnits(List<UnitsBean> units) {
                    this.units = units;
                }

                public static class UnitsBean {
                    private int unit_id;
                    private String unit_no;

                    public int getUnit_id() {
                        return unit_id;
                    }

                    public void setUnit_id(int unit_id) {
                        this.unit_id = unit_id;
                    }

                    public String getUnit_no() {
                        return unit_no;
                    }

                    public void setUnit_no(String unit_no) {
                        this.unit_no = unit_no;
                    }
                }
            }
        }
    }
}
