// Project of Dati e Algoritmi AA 2022-23

// @Author: Alessandro Rotondo (2032447)
// @Version: v. 1.1.0

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Scanner;

public class TopKFI{

	static PriorityQueue<IntPair> Q = new PriorityQueue<IntPair>(new IntPairComparator());
	static ArrayList<BitSet> singletonList = new ArrayList<BitSet>();
	static ArrayList<Integer []> transactions = new ArrayList<Integer []>();

	/* punto 3 */

	static ArrayList<IntPair> S = new ArrayList<IntPair>();
	
	public static void main(String args[]) {

		// parse input arguments
		if(args.length != 3){
			System.out.println("The arguments are not correct!");
			System.out.println("Please use \njava TopKFI datasetpath K M");
			return;
		}

		String db_path = args[0];
		int K = Integer.parseInt(args[1]);
		int M = Integer.parseInt(args[2]);

		if(K < 0 || M < 0){
			System.out.println("K and M should be positive!");
			return;
		}

		// read the input file
		try {
			File file_db = new File(db_path);
			try (Scanner db_reader = new Scanner(file_db)) {

				/* numero della riga che sto leggendo */

				int transaction_id = 1;

				while (db_reader.hasNextLine()) {
					String transaction = db_reader.nextLine();
					String[] items_str = transaction.split("\\s+");
					Integer[] items = new Integer[items_str.length];

					/* read the transaction "items_str" into the array "items" */

					for(int i=0; i<items_str.length; i++){
						try{
							items[i] = Integer.parseInt(items_str[i]);
						} catch (NumberFormatException e) {
							System.out.println("Input format of transaction is wrong!");
							System.out.println("transaction "+transaction_id+" is "+transaction);
							e.printStackTrace();
							return;
						}
					}

					/* punto 1 */

					for (Integer i : items) {
						try {
							singletonList.get(i).set(transaction_id);
						} catch (IndexOutOfBoundsException e) {
							for(int j = singletonList.size(); j < i + 1; j++) {
								singletonList.add(j, new BitSet());
							}
							singletonList.get(i).set(transaction_id);
						}
					}
					transaction_id++;
				}
			}
	    } catch (FileNotFoundException e) {
			System.out.println("The file "+db_path+" does not exist!");
			e.printStackTrace();
			return;
	    }

		/* punto 2 */

		for (int i = 0; i < singletonList.size(); i++) {
			if (!singletonList.get(i).isEmpty()) {
				Q.add(new IntPair(new ArrayList<Integer>(Arrays.asList(i)), singletonList.get(i)));
			}
		}

		/* pulizia di elementi inutili in Q */

		Integer Ktemp = 0;

		if (Q.size() > K) {
			PriorityQueue<IntPair> c = new PriorityQueue<IntPair>(Q);
			for (int i = 1; i < K; i++ ) {
				c.poll();
			}
			Ktemp = c.poll().set.cardinality();
		}

		final Integer KthSupport = Ktemp;

		/* 
		 * questa lista contiene gli elementi singoli utili per il calcolo di TopK-FI 
		 * tutti gli altri vanno buttati
		*/

		ArrayList<Integer> highSupportSingletons = new ArrayList<Integer>();
				
		Q.removeIf(elem -> (elem.set.cardinality() < KthSupport));

		Q.forEach(elem -> {
			highSupportSingletons.add(elem.value.get(0));
		});

		/* ordinamento della lista  */

		Collections.sort(highSupportSingletons);

		/* punto 4 */

		for (int i = 0; i < K; i++) {
			if (!Q.isEmpty()) {
				IntPair X = Q.poll();
				int a = X.value.get(X.value.size() - 1);

				S.add(X);

				int bIndex = binarySearch(highSupportSingletons, 0, highSupportSingletons.size(), a) + 1;
				
				for (; bIndex < highSupportSingletons.size(); bIndex++) {

					@SuppressWarnings("unchecked")
					IntPair Y = new IntPair((ArrayList<Integer>) X.value.clone(), (BitSet) X.set.clone());
					
					int b = highSupportSingletons.get(bIndex);
					evalSupport(Y, b);

					if(Y.set.cardinality() >= Ktemp) Q.add(Y);
				}
			} else {
				break;
			}

			/* pulizia di elementi inutili in Q */

			if (Q.size() > K) {
				
				Ktemp = 0;
				PriorityQueue<IntPair> c = new PriorityQueue<IntPair>(Q);
				for (int j = 1; j < K; j++ ) {
					c.poll();
				}
				Ktemp = c.poll().set.cardinality();
				
				final Integer MaxSupport = Ktemp;
					
				Q.removeIf(elem -> (elem.set.cardinality() < MaxSupport));
			}
		}

		/* pulizia di elementi inutili in Q */

		int Ksup = S.get(K - 1).set.cardinality();

		Q.removeIf(elem -> (elem.set.cardinality() < Ksup));

		/* punto 5 */

		if (!Q.isEmpty()){
			while (Q.peek().set.cardinality() <= Ksup) {
				if (!Q.isEmpty()) {
					IntPair X = Q.poll();
					int a = X.value.get(X.value.size() - 1);

					S.add(X);

					int bIndex = binarySearch(highSupportSingletons, 0, highSupportSingletons.size(), a) + 1;
					
					for (; bIndex < highSupportSingletons.size(); bIndex++) {

						@SuppressWarnings("unchecked")
						IntPair Y = new IntPair((ArrayList<Integer>) X.value.clone(), (BitSet) X.set.clone());
						
						int b = highSupportSingletons.get(bIndex);
						evalSupport(Y, b);

						if(Y.set.cardinality() == Ksup) Q.add(Y);
					}
				} else {
					break;
				}
				if (Q.isEmpty()) break;

				/* pulizia di elementi inutili in Q */

				if (Q.size() > K) {
				
					Ktemp = 0;
					PriorityQueue<IntPair> c = new PriorityQueue<IntPair>(Q);
					for (int j = 1; j < K; j++ ) {
						c.poll();
					}
					Ktemp = c.poll().set.cardinality();
					
					final Integer MaxSupport = Ktemp;
						
					Q.removeIf(elem -> (elem.set.cardinality() < MaxSupport));
				}
			}
		}
		
		/* punto 6  */
		
		System.out.println(S.size());

		/* punto 7 */
		
		if (S.size() <= M) {
			for (IntPair p : S) {
				System.out.println(p);
			}
		}
	}

	/* semplice ricerca binaria ricorsiva, algoritmo standard */

	static int binarySearch(ArrayList<Integer> list, int leftBorder, int rightBorder, int value) {
        if (rightBorder >= leftBorder) {
            int mid = leftBorder + (rightBorder - leftBorder) / 2;
            if (list.get(mid) == value)
                return mid;
            if (list.get(mid) > value)
                return binarySearch(list, leftBorder, mid - 1, value);
            return binarySearch(list, mid + 1, rightBorder, value);
        }
        return -1;
    }

	/* 
	 * metodo che calcola il support di un subset + un altro elemento
	 * tramite end bit-a-bit delle linee in cui sono contenuti entrambi
	 */

	static void evalSupport(IntPair Y, Integer b) {
		Y.set.and(singletonList.get(b));
		Y.value.add(b);
	}
}

/* 
 * fu classe che implementava una coppia di interi, 
 * ora e' un obrobrio molto veloce ;)
 */

class IntPair implements Comparable<IntPair> {

	/* salva i valori del subset */
	public ArrayList<Integer> value;  
	
	/* salva come true le linee del file di input in cui e' presente il subset */
	public BitSet set;

	public IntPair() {
		this.value = new ArrayList<Integer>();
		this.set = new BitSet();
	}

	public IntPair(ArrayList<Integer> value, BitSet set) {
		this.value = value;
		this.set = set;
	}

	@Override
	public int compareTo(IntPair o) {
		
		if (this.value.get(0) == o.value.get(0)) {
			return 0;
		} else if (this.value.get(0) < o.value.get(0)) {
			return -1;
		} else if (this.value.get(0) > o.value.get(0)) {
			return +1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		IntPair p = (IntPair) obj;
		if (this.value == p.value) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		String yeet = new String();
		for (Integer i : value) 
			yeet += i + " ";
		yeet += "(" + set.cardinality() + ")";		
		return yeet;
	}
}

/* comparator per ordinare la priority queue che contiene IntPair */

class IntPairComparator implements Comparator<IntPair> {

	@Override
	public int compare(IntPair arg0, IntPair arg1) {
		if (Integer.compare(arg0.set.cardinality(), arg1.set.cardinality()) != 0) {
			return Integer.compare(arg1.set.cardinality(), arg0.set.cardinality());
		} else{
			return arg0.compareTo(arg1);
		}
	}
}