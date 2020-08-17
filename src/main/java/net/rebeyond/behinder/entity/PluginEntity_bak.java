package net.rebeyond.behinder.entity;

public class PluginEntity_bak {
    private String comment;
    private String entryFile;
    private String icon;
    private boolean isGetShell;
    private String name;
    private String scriptType;
    private int type;

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public String getScriptType() {
        return this.scriptType;
    }

    public void setScriptType(String scriptType2) {
        this.scriptType = scriptType2;
    }

    public int getType() {
        return this.type;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String icon2) {
        this.icon = icon2;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public String getEntryFile() {
        return this.entryFile;
    }

    public void setEntryFile(String entryFile2) {
        this.entryFile = entryFile2;
    }

    public boolean isGetShell() {
        return this.isGetShell;
    }

    public void setGetShell(boolean getShell) {
        this.isGetShell = getShell;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment2) {
        this.comment = comment2;
    }
}
