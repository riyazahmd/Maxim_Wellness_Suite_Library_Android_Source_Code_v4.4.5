package com.maximintegrated.algorithms;

public class AlgorithmVersion {
    private String versionString;
    private int version;
    private int subVersion;
    private int subSubVersion;

    public AlgorithmVersion(String versionString, int version, int subVersion, int subSubVersion) {
        this.versionString = versionString;
        this.version = version;
        this.subVersion = subVersion;
        this.subSubVersion = subSubVersion;
    }

    public AlgorithmVersion() {
        versionString = "";
        version = 0;
        subVersion = 0;
        subSubVersion = 0;
    }

    public void set(char[] versionString, int version, int subVersion, int subSubVersion) {
        this.versionString = String.valueOf(versionString);
        this.version = version;
        this.subVersion = subVersion;
        this.subSubVersion = subSubVersion;
    }

    public String getVersionString() {
        return versionString;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getSubVersion() {
        return subVersion;
    }

    public void setSubVersion(int subVersion) {
        this.subVersion = subVersion;
    }

    public int getSubSubVersion() {
        return subSubVersion;
    }

    public void setSubSubVersion(int subSubVersion) {
        this.subSubVersion = subSubVersion;
    }
}
