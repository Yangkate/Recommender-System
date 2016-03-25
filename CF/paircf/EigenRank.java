package cf.paircf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cf.CF;
import cf.Grade;
import cf.UserGrade;

public class EigenRank extends CF {

	
	protected void calUserSimilar(UserGrade u, UserGrade v) {
		int nd = 0;
		
		int uID = u.getUserId();
    	int vID = v.getUserId();
		//discover the common grades
		List<Grade> uGrades = u.getGradeList();
		List<Grade> vGrades = v.getGradeList();
		Iterator<Grade> itU=uGrades.iterator();
		Iterator<Grade> itV=null;
        Grade uGrade,vGrade;
        int uItem;

		List<Grade> commonGradeU = new ArrayList<Grade>();
		List<Grade> commonGradeV = new ArrayList<Grade>();
		int uSize=uGrades.size();
        int vSize=vGrades.size();
        for(int i=0;i<uSize;i++){
			uGrade=uGrades.get(i);
			uItem=uGrade.getItemId();
			
			itV=vGrades.iterator();
			for(int j=0;j<vSize;j++){
				vGrade=vGrades.get(j);
				if(uItem==vGrade.getItemId()){
					commonGradeU.add(uGrade);
					commonGradeV.add(vGrade);
					break;
				}
			}
		}	
		
		//calculate the similarity between u and v
		Grade uPrev,vPrev,uPost,vPost;
		int commonCount = commonGradeU.size();
		for(int i=0;i<commonCount-1;i++){
			uPrev = commonGradeU.get(i);
			vPrev = commonGradeV.get(i);
			for(int j=i+1;j<commonCount;j++){
				uPost = commonGradeU.get(j);
				vPost = commonGradeV.get(j);				
				if((uPrev.getGrade()>uPost.getGrade()) && (vPrev.getGrade()<vPost.getGrade())){
					nd++;
				}else if((uPrev.getGrade()<uPost.getGrade()) && (vPrev.getGrade()>vPost.getGrade())){
					nd++;
				}				
			}
		}
		
		if(commonCount>1){
			float sim = (float)1-(float)((float)4*nd)/((float)commonCount*(commonCount-1));
			this.similarity[uID][vID] = sim;
			this.similarity[vID][uID] = sim;
		}		
		commonGradeU = null;
		commonGradeV = null;
		return;
	}

	
	protected List<Float> predictRating4User(UserGrade testUser) {
		//predict pairwise preferences
		List<Grade> testGradeList = testUser.getGradeList();
		int testGradeCount = testGradeList.size();
		
		float[][] predMatrix = new float[testGradeCount][testGradeCount];
		for(int i=0;i<testGradeCount-1;i++){
			Grade uPrev = testGradeList.get(i);
			for(int j=i+1;j<testGradeCount;j++){
				Grade uPost = testGradeList.get(j);
				float pred1 = predictGrade(testUser.getUserId(), uPrev.getItemId(), uPost.getItemId());
				predMatrix[i][j] = pred1;
				predMatrix[j][i] = -pred1;
			}
		}
		
		//aggregate preferences into a total order
		//initialization for aggregation
		float[] predList = new float[testGradeCount];
		int leftCount = testGradeCount;
		for(int i=0;i<testGradeCount;i++){
			float predOut= 0,predIn=0; 
			for(int j=0;j<testGradeCount;j++){
				if(i!=j)
					predOut = predOut + predMatrix[i][j];
			}
			for(int j=0;j<testGradeCount;j++){
				if(i!=j){
					predIn = predIn + predMatrix[j][i];
				}
			}
			predList[i] = predOut-predIn;
		}		
		
		//iteration
		List<Float> orderList = new ArrayList<Float>();
		List<Integer> leftList = new ArrayList<Integer>();
		for(int i=0;i<testGradeCount;i++){
			orderList.add((float)1);
			leftList.add(i);
		}
		
		while(leftList.size()>0){
			leftCount = leftList.size();
			if(leftCount <= 1){
				orderList.set(leftList.get(0),(float)1);
				leftList.remove(0);
				break;
			}
			
			int max = 0;
			int curIndex,maxIndex;
			for(int i=0;i<leftCount;i++){
				curIndex = leftList.get(i);
				maxIndex = leftList.get(max);
				if(predList[curIndex] > predList[maxIndex])
					max = i;
			}
			
			int index = leftList.get(max);
			orderList.set(index,(float)leftCount);
			leftList.remove(max);
			leftCount = leftList.size();
			for(int i=0;i<leftCount;i++){
				int indexLeft = leftList.get(i);
				predList[indexLeft] = predList[indexLeft] +predMatrix[index][indexLeft] - predMatrix[indexLeft][index];
			}
//				predList[i] = predList[i] - predMatrix[i][index];			
		}
		
		
		
		predList = null;
		predMatrix = null;
		leftList = null;
		return orderList;
	}
	
    private float predictGrade(int userID, int itemId1, int itemId2) {
    	UserGrade u = this.userGradeList.get(userID-1);
        
        List<Integer> neighbors = u.getNeighborList();
        int neighborCount=neighbors.size();
        int uID = u.getUserId();
        int vID;
        UserGrade v;
        float numerator = 0;
        float denominator = 0;
       for(int i=0;i<neighborCount;i++) {
        	vID = neighbors.get(i);
        	
            v = this.userGradeList.get(vID-1);
            
	        float sim = similarity[uID][vID];
	        double grade1 = v.hasRatedItem(itemId1);
	        double grade2 = v.hasRatedItem(itemId2);
	        if(grade1 > 0 && grade2 > 0){
	        	numerator += sim*(grade1-grade2);	        	
	            denominator += sim;
            }
        }
        
        float pref = 0;
        if(denominator!=0)
        	pref = numerator/denominator;

        return pref;
    }

}
