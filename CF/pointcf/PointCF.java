package cf.pointcf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cf.Grade;
import cf.UserGrade;

import cf.CF;

public class PointCF extends CF {

	public PointCF() {
		for (int i = 0; i < MAX_N_IN_NDCG; i++) {
			ndcg[i] = 0.0f;			
		}

	}

	public void calAvgRate() {
		Iterator<UserGrade> it = userGradeList.iterator();
		while (it.hasNext()) {
			UserGrade user = (UserGrade) it.next();
			user.calAvgRate();
		}
		return;
	}
	
    protected void calUserSimilar(UserGrade user1, UserGrade user2) {
    	float avgGrade1 = user1.getAvgRate();
    	float avgGrade2 = user2.getAvgRate();
    	int uID = user1.getUserId();
    	int vID = user2.getUserId();   	  		
    	
    	//find the common items
        
        List<Grade> uGradeList=user1.getGradeList();
        List<Grade> vGradeList=user2.getGradeList();

        Iterator<Grade> itU = uGradeList.iterator();
        Iterator<Grade> itV = vGradeList.iterator();
        Grade uGrade = itU.next();
        Grade vGrade = itV.next();
        
        int count = 0;
        float numerator = 0;
        float denominator1 = 0;
        float denominator2 = 0;
        while(itU.hasNext() && itV.hasNext()){
        	if(uGrade.getItemId() > vGrade.getItemId())
        		vGrade = itV.next();
        	else if(uGrade.getItemId() < vGrade.getItemId())
        		uGrade = itU.next();
        	else if(uGrade.getItemId() == vGrade.getItemId()){   
        		count++;
        		double difference1 = uGrade.getGrade() - avgGrade1;
        		double difference2 = uGrade.getGrade() - avgGrade2;
        		numerator += (difference1)*(difference2);
                denominator1 += Math.pow(difference1, 2);
                denominator2 += Math.pow(difference2, 2);           
                uGrade = itU.next();
                vGrade = itV.next();
        	}
        }
    	float sim=0;
        
        if(count>0){
			if (Math.sqrt(denominator1) * Math.sqrt(denominator2) < 0.000001) {
			       sim = 1;
		    } else {
			       sim = (float) (numerator / (Math.sqrt(denominator1) * Math
					.sqrt(denominator2)));
		    }
        }
       
        similarity[uID][vID] = sim;
        similarity[vID][uID] = sim;
    }


	private float predictGrade(int uID, int itemId) {
		UserGrade user = this.userGradeList.get(uID - 1);
		float avgUserGrade = user.getAvgRate();

		Iterator<Integer> iterator = user.getNeighborList().iterator();

		float numerator = 0;
		float denominator = 0;
		while (iterator.hasNext()) {
			int vID = iterator.next();
			// System.out.println(vID);
			UserGrade tmpUserGrade = this.userGradeList.get(vID - 1);

			float tmpSim = similarity[uID][vID];
			if (tmpUserGrade.hasRatedItem(itemId) >= 0) {
				double tmpGrade = tmpUserGrade.hasRatedItem(itemId);
				int tmpUserID = tmpUserGrade.getUserId();
				float avgTmpUserGrade = this.userGradeList.get(tmpUserID - 1)
						.getAvgRate();
				numerator += tmpSim * ((float) tmpGrade - avgTmpUserGrade);
				denominator += tmpSim;
			}
		}

		float rate = 3;
		if (denominator != 0)
			rate = avgUserGrade + numerator / denominator;

		if (rate < this.MIN_GRADE)
			rate = (float) this.MIN_GRADE;
		else if (rate > this.MAX_GRADE)
			rate = (float) this.MAX_GRADE;

		return rate;
	}

	protected List<Float> predictRating4User(UserGrade user) {
		List<Float> predList = new ArrayList<Float>();
		Iterator<Grade> itGrade = user.getGradeList().iterator();
		
		while (itGrade.hasNext()) {
			Grade grade = itGrade.next();
			float pred = predictGrade(user.getUserId(), grade.getItemId());

			predList.add(pred);
		}			
		return predList;
	}

}
