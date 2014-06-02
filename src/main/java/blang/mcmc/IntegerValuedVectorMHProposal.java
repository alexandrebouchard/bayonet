package blang.mcmc;

import java.util.List;
import java.util.Random;

import bayonet.distributions.Multinomial;
import blang.factors.Factor;
import blang.variables.IntegerValuedVector;
import blang.variables.RealVector;

/**
 * To be used for testing when the number of dimensions is small
 * Proposes new sample from Multinomial
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
public class IntegerValuedVectorMHProposal implements MHProposalDistribution 
{

	@SampledVariable IntegerValuedVector vector;
	@ConnectedFactor List<Factor> connectedFactors;

	private IntegerValuedVector savedVector;
	
	private static double [] probs = null;

	@Override
	public Proposal propose(Random rand) 
	{
		if (savedVector != null)
			throw new RuntimeException();
		
		savedVector = vector.deepCopy(); // make a deep copy

		// Idea: propose a new value from Multinomial with fixed parameters (pi_1 = pi_2 = ... = pi_K)
		// 1. generate a new vector from the Multinomial
		// 2. compute the log density of the newly generated vector(this is the denominator)
		// 3. compute the log density of the old value using the MLE obtained from the new realization in step 2 (this is the numerator)
		
		if (probs == null)
		{
			double pi = 1.0/vector.getDim();
			probs = RealVector.rep(vector.getDim(), pi).getVector();
		}

		int N = vector.componentSum(); // number of draws
		vector.setVector(Multinomial.generate(rand, N, probs));

		double logD = Multinomial.logDensity(vector, probs); // denominator
		double logN = Multinomial.logDensity(savedVector, probs); // numerator

		return new ProposalRealization(logN - logD);
	}

	private class ProposalRealization implements Proposal
	{
		private final double logRatio;
		public ProposalRealization(double logRatio)
		{
			this.logRatio = logRatio;
		}

	    @Override
	    public double logProposalRatio() { return logRatio; }
	
	    @Override
	    public void acceptReject(boolean accept)
	    {
	      if (!accept)
	      {
	        vector.setVector(savedVector.getVector());
	      }
	      savedVector = null;
	    }
	}

}