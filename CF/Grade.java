package cf;


public class Grade {
    private int itemId;
    private double grade;

    public Grade(int itemId, double grade) {
        this.itemId = itemId;
        this.grade = grade;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }
}
