package cf;

import cf.listcf.ListCF;
import cf.listcf.ListCF2_Time;
import cf.listcf.ListCF3_Time;
import cf.listcf.ListCF_New;

import cf.listcf.ListCF2;
import cf.pointcf.PointCF;
import cf.paircf.EigenRank;
import cf.paircf.PairCF;
import cf.paircf.VSRank;

public class Main {
    
	public static void main(String[] args) {
		String trainPath = "./data/movielens/train.txt";
		String testPath = "./data/movielens/test.txt";
		String simPath = null;
		String resultPath = null;
		String timePath = null;
			
//		simPath = "./results/movielens/point_similarity.txt";
//		resultPath = "./results/movielens/point_results.xls";
//		timePath = "./results/movielens/point_time.txt";
//		PointCF pointcf = new PointCF();
//		pointcf.run(trainPath, testPath, simPath, resultPath, timePath); 
//		pointcf = null;
		
//		PairCF paircf = new PairCF();
//		simPath = "./results/movielens/list_similarity.txt";
//		resultPath = "./results/movielens/list_results.xls";
//		timePath = "./results/movielens/list_time.txt";
//		paircf.run(trainPath, testPath, simPath, resultPath, timePath);
//		paircf = null;
		
//		simPath = "./results/movielens/eigenrank_similarity.txt";
//		resultPath = "./results/movielens/eigenrank_results.xls";
//		timePath = "./results/movielens/eigenrank_time.txt";
//		EigenRank er = new EigenRank();
//		er.run(trainPath, testPath, simPath, resultPath, timePath); 
		
//		simPath = "./results/movielens/vsrank_similarity.txt";
//		resultPath = "./results/movielens/vsrank_results.xls";
//		timePath = "./results/movielens/vsrank_time.txt";
//		VSRank vr = new VSRank();
//		vr.run(trainPath, testPath, simPath, resultPath, timePath);


		ListCF listcf = new ListCF();
		simPath = "./results/movielens/list_similarity.txt";
		resultPath = "./results/movielens/list_results.xls";
		timePath = "./results/movielens/list_time.txt";
		listcf.run(trainPath, testPath, simPath, resultPath, timePath);
		listcf = null;		
	}

}
