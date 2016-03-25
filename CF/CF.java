package cf;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;


//import jxl.*;
//import jxl.write.Label;
//import jxl.write.WritableSheet;
//import jxl.write.WritableWorkbook;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;



public abstract class CF {
	public enum CFType{POINT, LIST}
	private final String SEPERATOR = "\t"; 

	protected final int MAX_USER_COUNT = 6040;//ML:6040,EM:36656
	protected final int MAX_ITEM_ID=3952;//ML:3952,EM:1623
	protected final int neighbors=100;
	protected final int MIN_GRADE = 1;
	protected final int MAX_GRADE = 5;
	
	protected final int MAX_N_IN_NDCG = 10;
		
	protected int userCount = 0;
	public static int average_iteration=0;

	protected List<UserGrade> userGradeList;
	protected float[][] similarity = new float[MAX_USER_COUNT+1][MAX_USER_COUNT+1];
	protected float[] ndcg = new float[MAX_N_IN_NDCG+1];
    

    public CF() {
    }
    
 
    public void setUserGradeList(List<UserGrade> gradeList){
    	this.userGradeList = gradeList;
    }
    public int[] maxN(float[] data1,int k){ 
		float temp;  
		int temp1;
		int[] index=new int[data1.length];
		float[]data=new float[data1.length];
		System.arraycopy(data1, 0, data, 0, data1.length);
		int i;
		for(i=0;i<index.length;i++){
			index[i]=i;
		}
		for(i=0;i<data.length;i++){ 
		    for(int j=data.length-1;j>i;j--){
		        if(data[i]<data[j]){ 
		          temp =data[i];
		          data[i] = data[j];
		          data[j] = temp;
		          temp1=index[i];
		          index[i]=index[j];
		          index[j]=temp1;
		     }
		    }
		}	
		int[] indexN=new int[k];
		System.arraycopy(index, 0, indexN, 0, k);
		return indexN;
    }
    public void readSimilarity(String path){
    	System.out.println("Similarity Reading...");
    	File file = new File(path);
        BufferedReader reader = null;
        try {
        	reader = new BufferedReader(new FileReader(file));
        	String tempString = null;
        	
        	int ui = 0;
        	while ((tempString = reader.readLine()) != null){
        		if(ui>=userCount)
        			break;
        		String[] tempArray = tempString.split(", ");
        		for(int vi=0;vi<userCount;vi++){
        			float sim = Float.parseFloat(tempArray[vi]);
        			similarity[ui+1][vi+1] = sim;       			
        		}
        		ui++;
        		if(ui%100==0){
        			System.out.print("*");
        			if(ui%1000==0)
        				System.out.println();
        		}
        		
        	}
        	//find neighbors
	    	int[] neighborIndex=new int[neighbors];
	    	UserGrade userGrade=null;
	    	for(int i=0;i<userCount;i++){
	    		neighborIndex=maxN(similarity[i+1],neighbors);
	    		userGrade=this.userGradeList.get(i);
	    		for(int j=0;j<neighbors;j++){
	    			if(neighborIndex[j]==0){
	    				break;
	    			}
	    			userGrade.getNeighborList().add(neighborIndex[j]);
	    		}
	    	}
        	
        	System.out.println();
        } catch(Exception e) { 
        	e.printStackTrace();
        }
        return;
    }
    
    public List<UserGrade> read(String path) {
    	InputStream is=this.getClass().getResourceAsStream(path);   
        List<UserGrade> gradeList = new ArrayList<UserGrade>();
    	
        File file = new File(path);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            String tempString = null;           
            int curUserId = 0;
            UserGrade userGrade = null;
            Grade grade = null;
            int userID,itemID,rate;
            while ((tempString = reader.readLine()) != null) {
            	String[] tempArray = tempString.split(this.SEPERATOR);
               
                userID=Integer.parseInt(tempArray[0]);
                itemID=Integer.parseInt(tempArray[1]);
                rate=Integer.parseInt(tempArray[2]);

                if(userID != curUserId) {
                    if(userGrade != null && userGrade.getUserId() != 0) {
                    	userGrade.calAvgRate();
                    	gradeList.add(userGrade);
                    }

                    userGrade = new UserGrade();
                    userGrade.setUserId(userID);                    
                }

                grade = new Grade(itemID, rate);
                userGrade.getGradeList().add(grade);
                curUserId = userID;
            }
            
            userGrade.calAvgRate();
            gradeList.add(userGrade);
            
            reader.close();
            
            userCount = gradeList.size();
            System.out.println("User "+userCount);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }      
        return gradeList;
    }
    public List<UserGrade> fixTestNumber(int length) {
    	 
        List<UserGrade> gradeList = new ArrayList<UserGrade>();           

        Grade grade = null;
        for(int i=1;i<=MAX_USER_COUNT;i++) {
        	UserGrade userGrade=new UserGrade();
        	 userGrade.setUserId(i);
             for(int j=100;j<100+length;j++){
            	 grade = new Grade(j,4);
            	 userGrade.getGradeList().add(grade);
             }
             userGrade.calAvgRate();
             gradeList.add(userGrade);
        }

            userCount = gradeList.size();
            System.out.println("User "+userCount);
       
        return gradeList;
    }
    public static void appendFile(String fileName, String content) {  
        try {             
            FileWriter writer = new FileWriter(fileName, true);  
            writer.write(content);  
            writer.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }
    protected abstract void calUserSimilar(UserGrade user1, UserGrade user2);


    public long calSimilarity(String outputPath) {
    	long startTime = 0;
    	long endTime = 0;
  		
		
    	for(int i=0;i<=userCount;i++){
    		for(int j=0;j<=userCount;j++){
    			similarity[i][j]=0;
    		}
    	}
    	startTime=System.currentTimeMillis();
		for(int i=0;i<userCount;i++){   			
			UserGrade user1 = userGradeList.get(i);
			if(i%1000==0){
    			System.out.println(i);    			
    		}	
    		for(int j=i+1;j<userCount;j++){
    			UserGrade user2 = userGradeList.get(j);
    			this.calUserSimilar(user1, user2);
    		}
    		
    	}

        endTime=System.currentTimeMillis();
    	System.out.println();
    	
    	//find neighbors
    	System.out.println("finding neighbors...");
    	int[] neighborIndex=new int[neighbors];
    	UserGrade userGrade=null;
    	for(int i=0;i<userCount;i++){	    		
    		neighborIndex=maxN(similarity[i+1],neighbors);
    		userGrade=this.userGradeList.get(i);
    		for(int j=0;j<neighbors;j++){
    			if(neighborIndex[j]==0){
    				break;
    			}
    			if(similarity[i+1][neighborIndex[j]]<0){
    				break;
    			}
    			userGrade.getNeighborList().add(neighborIndex[j]);	    			
    		}	    		
    	}
    	try { 
            //create file and sheet
    		FileOutputStream fos= new FileOutputStream(outputPath);   
	    	
	    	System.out.println("Saving similarity to file...");
	    	
            for(int i=1;i<=userCount;i++){//context
	    	
	    		if(i%100==0){
	    			System.out.print("*");
	    			if(i%1000==0)
	    				System.out.println();	
	    		}
	    		
                for(int j=1;j<=userCount;j++){
                	if(i==j)
                		fos.write("0, ".getBytes());               	   
                	else              		
                		fos.write((""+similarity[i][j] + ", ").getBytes());               		
                }
                fos.write("\n".getBytes());
               
            }
            
            System.out.println();           
            fos.close();
           

        }
        catch(Exception e) { 
        	e.printStackTrace();
        } 
    	
    	long time = endTime - startTime;
    	return time;
    }    
    
    
//    private void calculateAP(int[] grandRank){
//    	float[] tempPATN = new float[MAX_N_IN_P_NDCG+1]; 
//    	for(int i=1;i<=MAX_N_IN_P_NDCG;i++){
//    		float val = grandRank[i-1];
//    		if(val>=4){
//	    		for(int j=i;j<=MAX_N_IN_P_NDCG;j++)
//	    			tempPATN[j] = tempPATN[j] + 1;
//    		}
//    	}
//    	float ap = 0;
//    	int relCount = 0;
//    	for(int i=1;i<=MAX_N_IN_P_NDCG;i++){
//    		tempPATN[i] = tempPATN[i]/i;
//    		this.patn[i] = patn[i] + tempPATN[i];
//    		if(grandRank[i-1]>=4){
//    			ap = ap + tempPATN[i];
//    			relCount++;
//    		}
//    	}
//    	
//    	if(relCount>0)
//    		ap = ap/relCount;
//    	
//    	map = map + ap;
//    	tempPATN = null;
//    	return;
//    }
    
    private float[] calculateDCG(int[] grandRank) {
		float[] dcg = new float[MAX_N_IN_NDCG + 1];
		for (int i = 1; i <= MAX_N_IN_NDCG; i++) {
			int gt = grandRank[i - 1];

			dcg[i] = dcg[i - 1] + ((float) Math.pow(2, gt) - 1)
					* (float) Math.log10(2) / (float) Math.log10(1 + i);
		}

		return dcg;
	}

	private void calculateNDCG(int[] predRank, int[] grandRank) {
		float[] dcg = calculateDCG(predRank); // Calculate DCG

		float[] maxDcg = calculateDCG(grandRank);

		for (int i = 1; i <= MAX_N_IN_NDCG; i++)
			ndcg[i] = ndcg[i] + dcg[i] / maxDcg[i];

		dcg = null;
		maxDcg = null;

		return;
	}
    
    private void calculateRanking(UserGrade testUser, List<Float> predList){
    	List<Integer> grandTruth = new ArrayList<Integer>();
    	int[] predRanked = new int[MAX_N_IN_NDCG];
    	int[] grandTruthRanked = new int[MAX_N_IN_NDCG];

    	Iterator<Grade> itUGradeList = testUser.getGradeList().iterator();
    	while(itUGradeList.hasNext()){
    		float grade = (float)itUGradeList.next().getGrade();
    		int gradeInt = (int)grade*10/10;
    		grandTruth.add(gradeInt);
    	}
    	
    	List<Float> tempPredList= new ArrayList<Float>();
    	Iterator<Float> itPredList = predList.iterator();
    	while(itPredList.hasNext())
    		tempPredList.add(itPredList.next());
    	
    	int predCount = predList.size();
    	
    	for(int i=0;i<MAX_N_IN_NDCG;i++){
    		for(int j=predCount-1;j>i;j--){
    			if(tempPredList.get(j)>tempPredList.get(j-1)){
    				float tempPred = tempPredList.get(j);
    				tempPredList.set(j, tempPredList.get(j-1));
    				tempPredList.set(j-1, tempPred);
    				
    				int tempTrue = grandTruth.get(j);
    				grandTruth.set(j, grandTruth.get(j-1));
    				grandTruth.set(j-1, tempTrue);
    			}
    		}
    		predRanked[i] = grandTruth.get(i);
    	}
        Collections.sort(grandTruth);
        for(int i=0;i<MAX_N_IN_NDCG;i++){
        	grandTruthRanked[i]=grandTruth.get(predCount-1-i);
        }
    	tempPredList = null;
    	//this.calculateAP(predRanked);
    	this.calculateNDCG(predRanked,grandTruthRanked);
    	grandTruth = null;
    	grandTruthRanked = null;
    	return;
    }
    
    
    protected abstract List<Float> predictRating4User(UserGrade testUser);
    
    public long predictRatings(List<UserGrade> testGradeList, String outputPath){
		
		long startTime = 0;
		long endTime = 0;
		
		try { 
            //create file and sheet
			
           // WritableWorkbook book= Workbook.createWorkbook(new File(outputPath));
           // WritableSheet sheet=book.createSheet("Results",0);
                                 			
			startTime=System.currentTimeMillis(); 
			Iterator<UserGrade> itTestUserGrade = testGradeList.iterator();
			while(itTestUserGrade.hasNext()){
				
				UserGrade testUser = itTestUserGrade.next();
				List<Float> predRatingList = predictRating4User(testUser);					
				
				this.calculateRanking(testUser, predRatingList);//NDCG@n
            	
				predRatingList = null;
			}																							
			
			int testUserCount = testGradeList.size();
			for(int i=1;i<=MAX_N_IN_NDCG;i++){	
				ndcg[i] = ndcg[i]/testUserCount;
			}
			endTime=System.currentTimeMillis();
			System.out.println("finished prediction");
							
        }
        catch(Exception e) { 
        	e.printStackTrace();
        } 
		
		long time = endTime - startTime; 
		return time;
    }
    
    public void run(String trainPath, String testPath, String simPath, String resultPath, String timePath){
		System.out.println("Training...");
		
		List<UserGrade> trainGradeList = read(trainPath);
		setUserGradeList(trainGradeList);
		long simTime = calSimilarity(simPath);
		System.out.println("Similarity takes "+simTime/1000+"s");
        //readSimilarity(simPath);
		
		System.out.println("Predicting...");
		List<UserGrade> testGradeList = read(testPath);

		long predTime = predictRatings(testGradeList, resultPath);
		
		
		//System.out.println("NDCG@n:");
		for(int i=1;i<=MAX_N_IN_NDCG;i++){
			System.out.println(String.valueOf(ndcg[i]));
			//System.out.print("@" + String.valueOf(i) + ": " + String.valueOf(ndcg[i]));
			//if(i<MAX_N_IN_NDCG)
				//System.out.print(", ");
		}
		System.out.println();

		System.out.println("Prediction takes "+predTime+"ms");

        
		trainGradeList = null;
		testGradeList = null;
    }
}
