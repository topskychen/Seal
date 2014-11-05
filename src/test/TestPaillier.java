package test;

import java.math.BigInteger;
import java.util.Random;

import timer.Timer;
import crypto.Paillier;

public class TestPaillier {

	public TestPaillier() {
		int times = 1000;
		Timer timer = new Timer();
		
		Paillier paillier = new Paillier(1280, 64);
		BigInteger mes = BigInteger.probablePrime(1020, new Random());
		
		BigInteger cipherPai = null;
		System.out.println("=============== Paillier encryption ================");
		timer.reset();
		for (int i = 0; i < times; i ++) {
			cipherPai = paillier.encrypt(mes);
		}
		timer.stop();
		System.out.println("Time Costs: " + timer.timeElapseinMs() / times + "ms");
		System.out.println("=====================================================");
		
		System.out.println("=============== Paillier decryption ================");
		timer.reset();
		for (int i = 0; i < times; i ++) {
			mes = paillier.decrypt(cipherPai);
		}
		timer.stop();
		System.out.println("Time Costs: " + timer.timeElapseinMs() / times + "ms");
	}

	public static void main(String[] args) {
		new TestPaillier();
	}
}
