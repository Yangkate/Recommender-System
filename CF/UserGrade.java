package cf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class UserGrade {
    protected int userId;
    private float avgRate;
    protected List<Grade> gradeList = new ArrayList<Grade>();   
    protected List<Integer> neighborList = new ArrayList<Integer>();

    public int getUserId() {
        return userId;
    }
    
    public float getAvgRate(){
    	return avgRate;
    }
    public float calAvgRate(){
    	int sumRate = 0;
        int itemCount = getGradeList().size();
        Iterator<Grade> it = getGradeList().iterator();
        while(it.hasNext()){
        	Grade grade = it.next();
        	sumRate += grade.getGrade();
        }

        avgRate = (float)sumRate/itemCount;
        return avgRate;
    }
	
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<Grade> getGradeList() {
        return gradeList;
    }

    public void setGradeList(List<Grade> gradeList) {
        this.gradeList = gradeList;
    }

    public List<Integer> getNeighborList() {
        return neighborList;
    }

    public void setNeighborList(List<Integer> neighborList) {
        this.neighborList = neighborList;
    }
    
    public double hasRatedItem(int itemID){
    	double rate = -1;
    	
    	Iterator<Grade> it = this.gradeList.iterator();
    	while(it.hasNext()){
    		Grade grade = it.next();
    		if(grade.getItemId() == itemID){
    			rate = grade.getGrade();
    			break;
    		}
    	}
    	
    	return rate;
    }
}
