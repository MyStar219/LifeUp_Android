package com.smartgateway.app.data.model.Apartment;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by Terry on 28/6/16.
 */
public class Apartments extends AbstractData{

    /**
     * default : true
     * status : register
     * condo : {"building":{"building_id":1,"block":"Block 01","level":{"unit":{"unit_id":1,"unit_no":"20-2-21"},"level":2}},"condo_id":2,"name":"Yishun Sapphire"}
     * apartment_id : 1
     */
    private String detail;

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    private List<ApartmentsBean> Apartments;

    public List<ApartmentsBean> getApartments() {
        return Apartments;
    }

    public void setApartments(List<ApartmentsBean> Apartments) {
        this.Apartments = Apartments;
    }

    public static class ApartmentsBean {
        @SerializedName("default")
        private boolean defaultX;
        private String status;
        /**
         * building : {"building_id":1,"block":"Block 01","level":{"unit":{"unit_id":1,"unit_no":"20-2-21"},"level":2}}
         * condo_id : 2
         * name : Yishun Sapphire
         */

        private CondoBean condo;
        private int apartment_id;

        public boolean isDefaultX() {
            return defaultX;
        }

        public void setDefaultX(boolean defaultX) {
            this.defaultX = defaultX;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public CondoBean getCondo() {
            return condo;
        }

        public void setCondo(CondoBean condo) {
            this.condo = condo;
        }

        public int getApartment_id() {
            return apartment_id;
        }

        public void setApartment_id(int apartment_id) {
            this.apartment_id = apartment_id;
        }

        public static class CondoBean {
            /**
             * building_id : 1
             * block : Block 01
             * level : {"unit":{"unit_id":1,"unit_no":"20-2-21"},"level":2}
             */

            private BuildingBean building;
            private int condo_id;
            private String name;

            public BuildingBean getBuilding() {
                return building;
            }

            public void setBuilding(BuildingBean building) {
                this.building = building;
            }

            public int getCondo_id() {
                return condo_id;
            }

            public void setCondo_id(int condo_id) {
                this.condo_id = condo_id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public static class BuildingBean {
                private int building_id;
                private String block;
                /**
                 * unit : {"unit_id":1,"unit_no":"20-2-21"}
                 * level : 2
                 */

                private LevelBean level;

                public int getBuilding_id() {
                    return building_id;
                }

                public void setBuilding_id(int building_id) {
                    this.building_id = building_id;
                }

                public String getBlock() {
                    return block;
                }

                public void setBlock(String block) {
                    this.block = block;
                }

                public LevelBean getLevel() {
                    return level;
                }

                public void setLevel(LevelBean level) {
                    this.level = level;
                }

                public static class LevelBean {
                    /**
                     * unit_id : 1
                     * unit_no : 20-2-21
                     */

                    private UnitBean unit;
                    private int level;

                    public UnitBean getUnit() {
                        return unit;
                    }

                    public void setUnit(UnitBean unit) {
                        this.unit = unit;
                    }

                    public int getLevel() {
                        return level;
                    }

                    public void setLevel(int level) {
                        this.level = level;
                    }

                    public static class UnitBean {
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
}
