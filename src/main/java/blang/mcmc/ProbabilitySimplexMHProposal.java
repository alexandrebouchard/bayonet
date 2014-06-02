package blang.mcmc;

import java.util.List;
import java.util.Random;

import bayonet.distributions.Dirichlet;
import blang.factors.Factor;
import blang.variables.ProbabilitySimplex;
import blang.variables.RealVector;

/**
 * For testing ProbabilitySimplex data type when the number of dimensions is small
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
public class ProbabilitySimplexMHProposal implements MHProposalDistribution
{
	@SampledVariable ProbabilitySimplex probSimplex;
	@ConnectedFactor List<Factor> connectedFactors;

	private ProbabilitySimplex probSaved = null;
	
	@Override
	public Proposal propose(Random rand) 
	{
		if (probSaved != null)
			throw new RuntimeException();

		probSaved = probSimplex.deepCopy();

		// Propose a new probability simplex from a symmetric Dirichlet, this will lead to log proposal ratio of 0
		int dim = probSimplex.getDim();
		double [] piNew = Dirichlet.generate(rand, RealVector.ones(dim).getVector());
		probSimplex.setVector(piNew);

		return new ProposalRealization();
  }

	private class ProposalRealization implements Proposal
	{

		@Override
		public double logProposalRatio() 
		{
		    return 0;
		}
		
		@Override
		public void acceptReject(boolean accept) 
		{
		    if (!accept)
		    {
		    	// set the value back
		    	probSimplex.setVector(probSaved.getVector());
		    }
		    	
		    probSaved = null;
		}
		
	}

}