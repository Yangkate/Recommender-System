package cf.listcf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cf.CF;
import cf.Grade;
import cf.UserGrade;

public class ListCF extends CF{
	private final int MIN_COMMON_ITEM = 6;
	private final float GRADIENT_THESHOLD = (float)0.0001;
	private final float DIFFERENCE_THESHOLD = (float)0.0001;
	private float INIT_LEARNING_RATE = (float)2;
	private float DISCREATE_LEARNING_RATE = (float)1;
	
	private int MAX_ITERATION = 100;
	private final double INIT_RATING = 3;
	
	public float getInitLearningRate(){
		return INIT_LEARNING_RATE;
	}
	
	public void setInitLearningRate(float lr){
		INIT_LEARNING_RATE = lr;
	}
	
	public void setMaxIteration(int max){
		MAX_ITERATION = max;
	}
	

	protected void calUserSimilar(UserGrade user1, UserGrade user2) {
    	double sim_uv = 1;
    	int uID = user1.getUserId();
    	int vID = user2.getUserId();   	  		
    	
    	//find the common items
        List<Float> uProbDist = new ArrayList<Float>();
        List<Float> vProbDist = new ArrayList<Float>();
        float uProbSum = 0;
        float vProbSum = 0;
        
        List<Grade> uGradeList=user1.getGradeList();
        List<Grade> vGradeList=user2.getGradeList();

        Iterator<Grade> itU = uGradeList.iterator();
        Iterator<Grade> itV = vGradeList.iterator();
        Grade uGrade = itU.next();
        Grade vGrade = itV.next();
        while(itU.hasNext() && itV.hasNext()){
        	if(uGrade.getItemId() > vGrade.getItemId())
        		vGrade = itV.next();
        	else if(uGrade.getItemId() < vGrade.getItemId())
        		uGrade = itU.next();
        	else if(uGrade.getItemId() == vGrade.getItemId()){
    		    float uProb = (float)gradeMapping(uGrade.getGrade()); 
                float vProb = (float)gradeMapping(vGrade.getGrade());
                uProbDist.add(uProb);
                vProbDist.add(vProb);
                uProbSum = uProbSum + uProb;
                vProbSum = vProbSum + vProb;
                uGrade = itU.next();
                vGrade = itV.next();
        	}
        }
        //calculate the similarity from user1 to user2 and from user2 to user1
        int commonRatedNum=uProbDist.size();
        
        for(int i=0;i<commonRatedNum;i++){
        	float uProb=uProbDist.get(i)/uProbSum;
        	float vProb=vProbDist.get(i)/vProbSum;
        	sim_uv = sim_uv - 0.5*vProb*(float)Math.log(vProb/uProb)/(float)Math.log(2)-0.5*uProb*(float)Math.log(uProb/vProb)/(float)Math.log(2);
        
        }

        similarity[uID][vID] = (float) sim_uv*((float)commonRatedNum/MIN_COMMON_ITEM);
//        similarity[uID][vID] = (float) sim_uv;
//        if(commonRatedNum<MIN_COMMON_ITEM){
//        	similarity[uID][vID]=similarity[uID][vID]*((float)commonRatedNum/MIN_COMMON_ITEM);
//        }
        similarity[vID][uID] = similarity[uID][vID];
        
        uProbDist = null;
        vProbDist = null;     
        
	}

	protected List<Float> predictRating4User(UserGrade testUser) {
		float learningRate = INIT_LEARNING_RATE;
		
		List<Float> predList = new ArrayList<Float>();
		List<Float> prePredList = new ArrayList<Float>();
		
		List<Grade> uGrades = testUser.getGradeList();
		Iterator<Grade> itUGrades=uGrades.iterator();
		int gradeCount = uGrades.size();//number of items in the test set for testUser
		
		int uID = testUser.getUserId();		
		UserGrade trainUser = this.userGradeList.get(uID-1);

		for(int i=0;i<gradeCount;i++){
			predList.add((float)gradeMapping(INIT_RATING));
			prePredList.add((float)gradeMapping(INIT_RATING));
		}

		Iterator<Grade> itVGrades=null;
		float[] diviative = new float[gradeCount];
		float[] first = new float[gradeCount];
		float[] second = new float[gradeCount];
		
		
		int itemID;
		Grade uGrade=null,vGrade=null;

		List<Integer> neighbors= trainUser.getNeighborList();
		int neighborCount=neighbors.size();
		boolean[][] isVGrade=new boolean[neighborCount][gradeCount];
		float[][] vProbDivU = new float[neighborCount][gradeCount];
		for(int i=0;i<neighborCount;i++){
			int vID=neighbors.get(i);
			UserGrade v = this.userGradeList.get(vID-1);
			List<Grade> vGrades = v.getGradeList();
			itUGrades=uGrades.iterator();
			int indexUItem = -1;
			for(int j=0;j<gradeCount;j++){
				vProbDivU[i][j]=0f;
				isVGrade[i][j]=false;
				
			}
			while(itUGrades.hasNext()){
				indexUItem++;
				uGrade=itUGrades.next();
				itemID=uGrade.getItemId();
				itVGrades=vGrades.iterator();
				while(itVGrades.hasNext()){
					vGrade=itVGrades.next();
					if(vGrade.getItemId()==itemID){
						isVGrade[i][indexUItem]=true;
						vProbDivU[i][indexUItem]=(float)gradeMapping(vGrade.getGrade());
						break;
					}
				}					
			}
			
		}
		
		for(int i=0;i<MAX_ITERATION;i++){
	
			//itNeighbor = trainUser.getNeighborList().iterator();			
			//the first part of the gradient
			//the second part of the gradient						
			for(int j=0;j<gradeCount;j++){
				first[j]=0f;
				second[j]=0f;
				diviative[j]=0f;
			}
			
			for(int j=0;j<neighborCount;j++){
				
				int vID = neighbors.get(j);				
				float sim = this.similarity[uID][vID];
				
				//find the set of items T_v = T \cap I_v				
				itUGrades=uGrades.iterator();
	
				
				float uPredSum = 0f;
				float vPredSum = 0f;
                for(int k=0;k<gradeCount;k++){
                	if(isVGrade[j][k]){
                		float uPred = predList.get(k);
						uPredSum=uPredSum+uPred;
						vPredSum=vPredSum+ vProbDivU[j][k];
                	}
                }
              //calculate the first and second parts of the gradient
				for(int k=0;k<gradeCount;k++){
					if(isVGrade[j][k]){
						first[k] = first[k] + sim/uPredSum;
						second[k] = second[k] + sim*(vProbDivU[j][k]/vPredSum)/predList.get(k);
					}
				}													
			}
			
			
			//update
			
			for(int predIndex=0; predIndex<gradeCount; predIndex++){
				float curPred = predList.get(predIndex);				
				diviative[predIndex] = first[predIndex] - second[predIndex];
				float nextPred = curPred - learningRate * diviative[predIndex];
				if(nextPred < gradeMapping(this.MIN_GRADE))
					nextPred = (float) gradeMapping(this.MIN_GRADE);

				prePredList.set(predIndex, curPred);
				predList.set(predIndex, nextPred);				
			}		


			//decide whether determine the loop
 			float difTotal = 0f;
			//float divTotal = 0;
			for(int predIndex=0; predIndex<gradeCount; predIndex++){
				float curPred = predList.get(predIndex);
				float prePred = prePredList.get(predIndex);
				float dif = curPred - prePred;				
				difTotal = difTotal + dif*dif;				
				//divTotal = divTotal + diviative[predIndex]*diviative[predIndex];								
			}
			//divTotal = (float)Math.sqrt(divTotal);
			
			difTotal = (float)Math.sqrt(difTotal);
			learningRate = learningRate*DISCREATE_LEARNING_RATE;

			if(difTotal <= DIFFERENCE_THESHOLD)
				break;
		}		
		diviative = null;
		first = null;
		second = null;
		
		return predList;
	}
	
	
	protected double gradeMapping(double grade) {
		return Math.exp(grade);
		//return grade;
	}	
}
