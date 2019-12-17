package permission;

public enum Perm {
    ADD("W"),
    READ("R"),
    EDIT("E");
    private String right;
    Perm(String right){
        this.right = right;
    }

    public String getRight() {
        return right;
    }
}
