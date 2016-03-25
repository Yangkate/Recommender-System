package cf.paircf;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cf.CF;
import cf.Grade;
import cf.UserGrade;

public class VSRank extends CF { 
	public long calSimilarity(String simPath) {

		float[] uWeight=new float[this.userCount+1];
		for (int i = 0; i <= userCount; i++) {
			uWeight[i]=0;
			for (int j = 0; j <= userCount; j++) {
				similarity[i][j] = 0;
			}
		}

		int[][] preferenceUser = new int[MAX_ITEM_ID + 1][MAX_ITEM_ID + 1];
		for (int i = 0; i <= MAX_ITEM_ID; i++) {
			for (int j = 0; j <= MAX_ITEM_ID; j++) {
				preferenceUser[i][j] = 0;
			}
		}
		long startTime =System.currentTimeMillis();
		UserGrade user;
		List<Grade> gradeList = null;
		int item1, item2;
		double rating1, rating2;
		for (int i = 0; i < userCount; i++) {
			user = userGradeList.get(i);
			gradeList = user.getGradeList();
			int gradeCount = gradeList.size();
			for (int j = 0; j < gradeCount - 1; j++) {
				item1 = gradeList.get(j).getItemId();
				rating1 = gradeList.get(j).getGrade();
				for (int k = j + 1; k < gradeCount; k++) {
					item2 = gradeList.get(k).getItemId();
					rating2 = gradeList.get(k).getGrade();
					if (rating1 > rating2) {
						preferenceUser[item1][item2]++;
					}
					if (rating2 > rating1) {
						preferenceUser[item2][item1]++;
					}
				}
			}

		}

		// calculate specialty
		float[][] specialty = new float[MAX_ITEM_ID + 1][MAX_ITEM_ID + 1];
		for (int i = 1; i <= MAX_ITEM_ID; i++) {
			for (int j = 1; j <= MAX_ITEM_ID; j++) {
				if (preferenceUser[i][j] != 0 && preferenceUser[j][i] != 0) {
					specialty[i][j] = (float) (Math
							.log((double) (preferenceUser[i][j] + preferenceUser[j][i])
									/ preferenceUser[i][j]) / Math
							.log((double) 2));
				} else {
					specialty[i][j] = 0;
				}
			}
		}
		double degree = 0;
		float special = 0;
		float weight = 0;
		for (int i = 0; i < userCount; i++) {
			weight=0;
			user = userGradeList.get(i);
			gradeList = user.getGradeList();
			int gradeCount = gradeList.size();
			for (int j = 0; j < gradeCount - 1; j++) {
				item1 = gradeList.get(j).getItemId();
				rating1 = gradeList.get(j).getGrade();
				for (int k = j + 1; k < gradeCount; k++) {
					item2 = gradeList.get(k).getItemId();
					rating2 = gradeList.get(k).getGrade();
					if (rating1 > rating2) {
						degree = Math.log(1 + Math.abs(rating1-rating2))
								/ Math.log((double) 2);
						special = specialty[item1][item2];
						weight+=Math.pow(degree*special,2);
					}else if(rating1 < rating2){
						degree = Math.log(1 + Math.abs(rating1-rating2))
								/ Math.log((double) 2);
						special = specialty[item2][item1];
						weight+=Math.pow(degree*special,2);
					}
				}
			}
			uWeight[i+1]=(float) Math.sqrt(weight);

		}
		
		
		System.out.println("Calculating similarities.");
		for (int i = 0; i < userCount; i++) {
			UserGrade user1 = userGradeList.get(i);

			
				if (i % 1000 == 0)
					System.out.println("*");
			

			for (int j = i + 1; j < userCount; j++) {
				UserGrade user2 = userGradeList.get(j);
				this.calUserSimilar(user1, user2, specialty,uWeight);
			}			
		}

		long endTime = System.currentTimeMillis();
		System.out.println();

		// find neighbors
		int[] neighborIndex = new int[neighbors];
		UserGrade userGrade = null;
		for (int i = 0; i < userCount; i++) {
			neighborIndex = maxN(similarity[i+1], neighbors);
			userGrade = this.userGradeList.get(i);
			for (int j = 0; j < neighbors; j++) {
				if (neighborIndex[j] == 0) {
					break;
				}
				userGrade.getNeighborList().add(neighborIndex[j]);
			}
		}

		System.out.println("Saving similarity to file...");
		try {
			// create file and sheet
			FileOutputStream fos = new FileOutputStream(simPath);
			for (int i = 1; i <= userCount; i++) {// context
				if (i % 100 == 0) {
					System.out.print("*");
					if (i % 1000 == 0)
						System.out.println();
				}

				for (int j = 1; j <= userCount; j++) {
					if (i == j)
						fos.write("0, ".getBytes());
					else
						fos.write((String.valueOf(similarity[i][j]) + ", ")
								.getBytes());
				}

				fos.write("\n".getBytes());
			}

			System.out.println();

			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return endTime - startTime;

	}

	protected void calUserSimilar(UserGrade user1, UserGrade user2) {
	};

	protected void calUserSimilar(UserGrade u, UserGrade v, float[][] specialty,float[] uWeight) {		
		List<Grade> uGrades = u.getGradeList();
		List<Grade> vGrades = v.getGradeList();


		// discover the common grades

		Iterator<Grade> itU = uGrades.iterator();
		Iterator<Grade> itV = null;
		Grade uGrade, vGrade;
		int uItem;

		List<Grade> commonGradeU = new ArrayList<Grade>();
		List<Grade> commonGradeV = new ArrayList<Grade>();

		while (itU.hasNext()) {
			uGrade = itU.next();
			uItem = uGrade.getItemId();

			itV = vGrades.iterator();
			while (itV.hasNext()) {
				vGrade = itV.next();
				if (uItem == vGrade.getItemId()) {
					commonGradeU.add(uGrade);
					commonGradeV.add(vGrade);
					break;
				}
			}
		}
				
		
		// calculate numerator
		int index = -1;
		double degree = 0;
		float special = 0;
		int itemPrev = 0, itemPost = 0;
		int commonCount = commonGradeU.size();
		float[] uCommonWeight = new float[commonCount * (commonCount - 1) / 2];
		float[] vCommonWeight = new float[commonCount * (commonCount - 1) / 2];
		Grade uPrev, vPrev, uPost, vPost;
		for (int i = 0; i < commonCount - 1; i++) {
			uPrev = commonGradeU.get(i);
			vPrev = commonGradeV.get(i);
			itemPrev = uPrev.getItemId();
			for (int j = i + 1; j < commonCount; j++) {
				uPost = commonGradeU.get(j);
				vPost = commonGradeV.get(j);
				itemPost = uPost.getItemId();
				index = index + 1;
				if (uPrev.getGrade() > uPost.getGrade()) {
					degree = Math.log(1 + Math.abs(uPrev.getGrade()
							- uPost.getGrade()))
							/ Math.log((double) 2);
					special = specialty[itemPrev][itemPost];
					uCommonWeight[index] = (float) (degree * special);

				} else if (uPrev.getGrade() < uPost.getGrade()) {
					degree = -Math.log(1 + Math.abs(uPrev.getGrade()
							- uPost.getGrade()))
							/ Math.log((double) 2);
					special = specialty[itemPost][itemPrev];
					uCommonWeight[index] = (float) (degree * special);
				} else {
					degree = 0;
					uCommonWeight[index] = 0;
				}

				if (vPrev.getGrade() > vPost.getGrade()) {
					degree = Math.log(1 + Math.abs(vPrev.getGrade()
							- vPost.getGrade()))
							/ Math.log((double) 2);
					special = specialty[itemPrev][itemPost];
					vCommonWeight[index] = (float) (degree * special);

				} else if (vPrev.getGrade() < vPost.getGrade()) {
					degree = -Math.log(1 + Math.abs(vPrev.getGrade()
							- vPost.getGrade()))
							/ Math.log((double) 2);
					special = specialty[itemPost][itemPrev];
					vCommonWeight[index] = (float) (degree * special);
				} else {
					degree = 0;
					vCommonWeight[index] = 0;
				}
			}
		}

		// calculate the similarity between u and v
		float numerator = 0;
		for (int i = 0; i < commonCount * (commonCount - 1) / 2; i++) {
			numerator += uCommonWeight[i] * vCommonWeight[i];
		}

		int uID = u.getUserId();
		int vID = v.getUserId();
		float sim = 0;
		if (commonCount > 1&& uWeight[uID]>0 && uWeight[vID]>0) {
			sim = (float) (numerator / (uWeight[uID]* uWeight[vID]));

			this.similarity[uID][vID] = sim;
			this.similarity[vID][uID] = sim;
		}

		commonGradeU = null;
		commonGradeV = null;
		return;
	}

	protected List<Float> predictRating4User(UserGrade testUser) {
		// predict pairwise preferences
		List<Grade> testGradeList = testUser.getGradeList();
		int testGradeCount = testGradeList.size();

		float[][] predMatrix = new float[testGradeCount][testGradeCount];
		for (int i = 0; i < testGradeCount - 1; i++) {
			Grade uPrev = testGradeList.get(i);
			for (int j = i + 1; j < testGradeCount; j++) {
				Grade uPost = testGradeList.get(j);
				float pred1 = predictGrade(testUser.getUserId(),
						uPrev.getItemId(), uPost.getItemId());
				predMatrix[i][j] = pred1;
				predMatrix[j][i] = -pred1;
			}
		}

		// aggregate preferences into a total order
		// initialization for aggregation
		float[] predList = new float[testGradeCount];
		int leftCount = testGradeCount;
		for (int i = 0; i < testGradeCount; i++) {
			float predOut = 0, predIn = 0;
			for (int j = 0; j < testGradeCount; j++) {
				if (i != j)
					predOut = predOut + predMatrix[i][j];
			}
			for (int j = 0; j < testGradeCount; j++) {
				if (i != j) {
					predIn = predIn + predMatrix[j][i];
				}
			}
			predList[i] = predOut - predIn;
		}

		// iteration
		List<Float> orderList = new ArrayList<Float>();
		List<Integer> leftList = new ArrayList<Integer>();
		for (int i = 0; i < testGradeCount; i++) {
			orderList.add((float) 1);
			leftList.add(i);
		}

		while (leftList.size() > 0) {
			leftCount = leftList.size();
			if (leftCount <= 1) {
				orderList.set(leftList.get(0), (float) 1);
				leftList.remove(0);
				break;
			}

			int max = 0;
			int curIndex, maxIndex;
			for (int i = 0; i < leftCount; i++) {
				curIndex = leftList.get(i);
				maxIndex = leftList.get(max);
				if (predList[curIndex] > predList[maxIndex])
					max = i;
			}

			int index = leftList.get(max);
			orderList.set(index, (float) leftCount);
			leftList.remove(max);
			leftCount = leftList.size();
			for (int i = 0; i < leftCount; i++) {
				int indexLeft = leftList.get(i);
				predList[indexLeft] = predList[indexLeft]
						+ predMatrix[index][indexLeft]
						- predMatrix[indexLeft][index];
			}
			// predList[i] = predList[i] - predMatrix[i][index];
		}

		predList = null;
		predMatrix = null;
		leftList = null;
		return orderList;
	}

	private float predictGrade(int userID, int itemId1, int itemId2) {
		UserGrade u = this.userGradeList.get(userID - 1);

		List<Integer> neighbors = u.getNeighborList();
        int neighborCount=neighbors.size();
        int uID = u.getUserId();
        int vID;
        UserGrade v;
        float numerator = 0;
        float denominator = 0;
       for(int i=0;i<neighborCount;i++) {
			vID = neighbors.get(i);

			v = this.userGradeList.get(vID - 1);

			float sim = similarity[uID][vID];
			double grade1 = v.hasRatedItem(itemId1);
			double grade2 = v.hasRatedItem(itemId2);

			if(grade1 > 0 && grade2 > 0){				
	        	numerator += sim*(grade1-grade2);	        	
	            denominator += sim;
            }
		}

		float pref = 0;
		if (denominator != 0)
			pref = numerator / denominator;

		return pref;
	}

	public void run(String trainPath, String testPath, String simPath,
			String resultPath, String timePath) {
		System.out.println("Training...");
		List<UserGrade> trainGradeList = read(trainPath);
		
		setUserGradeList(trainGradeList);
		long simTime = calSimilarity(simPath);

		 //readSimilarity(simPath);

		System.out.println("Predicting...");
		List<UserGrade> testGradeList = read(testPath);
		long predTime = predictRatings(testGradeList, resultPath);

		System.out.println("NDCG@n:");
		for (int i = 1; i <= MAX_N_IN_NDCG; i++) {
			System.out.print("@" + String.valueOf(i) + ": "
					+ String.valueOf(ndcg[i]));
			if (i < MAX_N_IN_NDCG)
				System.out.print(", ");
		}
		System.out.println();
		System.out.println("Similarity calculation takes "+simTime);
		System.out.println("Prediction takes "+predTime);
		try {
			FileOutputStream fos = new FileOutputStream(timePath);
			fos.write("Similarity calculation takes ".getBytes());
	        fos.write(String.valueOf(simTime/1000).getBytes());
	        fos.write(" s\n\n".getBytes());
	        
			fos.write("Prediction takes ".getBytes());
	        fos.write(String.valueOf(predTime/1000).getBytes());
	        fos.write(" s\n\n".getBytes());
	        fos.close();
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
