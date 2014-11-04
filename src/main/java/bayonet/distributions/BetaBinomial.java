package bayonet.distributions;

import java.util.Random;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;

import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.GenerativeFactor;
import blang.variables.IntegerVariable;
import blang.variables.RealVariable;
import static org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficientLog;
import static org.apache.commons.math3.special.Beta.logBeta;
import static blang.variables.IntegerVariable.intVar;
import static blang.variables.RealVariable.real;

/**
 * 
 * 
 * @author Sean Jewell (jewellsean@gmail.com)
 *
 */

public class BetaBinomial <P extends BetaBinomial.Parameters> implements GenerativeFactor, UnivariateIntegerDistribution
{
    /** 
     * The variable on which this density is defined
     */
    @FactorArgument(makeStochastic=true)
    public final IntegerVariable realization; 
    
    /**
     * Parameters of BetaBinomial density
     */
    @FactorComponent
    public final P parameters; 
    
    public static interface Parameters
    {
      public int getTrials();
      public double getAlpha(); 
      public double getBeta();
    }
    
    
    public static class AlphaBetaParameterization implements Parameters
    {
        @FactorArgument
        public final RealVariable alpha; 
        @FactorArgument
        public final RealVariable beta; 
        @FactorArgument
        public final IntegerVariable trials;
        
        public AlphaBetaParameterization(RealVariable alpha, RealVariable beta, IntegerVariable trials) 
        {
            this.alpha = alpha;
            this.beta = beta;
            this.trials = trials;
        }             
        
        @Override
        public int getTrials() {
            return trials.getIntegerValue();
        }
        @Override
        public double getAlpha() {
            return alpha.getValue();
        }
        @Override
        public double getBeta() {
            return beta.getValue();
        } 
        
    }
    
    public BetaBinomial(IntegerVariable realization, P parameters)
    {
        this.realization = realization; 
        this.parameters = parameters;
    }

    @Override
    public double logDensity()
    {
      return logDensity(realization.getIntegerValue(), parameters.getAlpha(), parameters.getBeta(), parameters.getTrials());
    }

    @Override
    public IntegerVariable getRealization()
    {
     return realization;
    }

    @Override
    // generate iteratively? 
    // passed KS tests in R 
    public void generate(Random random)
    {
      BetaDistribution beta = new BetaDistribution(new Random2RandomGenerator(random),
              parameters.getAlpha(), parameters.getBeta());
      double p = beta.sample();
      BinomialDistribution binomial = new BinomialDistribution(new Random2RandomGenerator(random),
              parameters.getTrials(), p);
      realization.setValue(binomial.sample());
    }
    
    
    /* Static versions of the functionalities of this class */

    public static double logDensity(int point, double alpha, double beta, int trials)
    {
        if (alpha < 0 || beta < 0 || trials < 1 || point < 0 || point > trials)
            return Double.NEGATIVE_INFINITY;
        double logDensity = binomialCoefficientLog(trials, point);
        logDensity += logBeta(point + alpha, trials - point + beta);
        logDensity -= logBeta(alpha, beta);
        return logDensity;
    }
    
    public static BetaBinomial<AlphaBetaParameterization> on(IntegerVariable realization)
    {
        return new BetaBinomial<AlphaBetaParameterization>(realization, 
                new AlphaBetaParameterization(real(0.5), real(0.5), intVar(1)));
    }
    
    public BetaBinomial<AlphaBetaParameterization> withAlphaBetaN(RealVariable alpha, RealVariable beta, IntegerVariable trials)
    {
        return new BetaBinomial<AlphaBetaParameterization>(realization, new AlphaBetaParameterization(alpha, beta, trials));
    }
    
    public BetaBinomial<AlphaBetaParameterization> withAlphaBetaN(double alpha, double beta, int trials)
    {
        return new BetaBinomial<AlphaBetaParameterization>(realization, new AlphaBetaParameterization(real(alpha), real(beta), intVar(trials)));
    }
    
}