package jbui.persistence;

abstract class ResultsHandlingDAO extends DAO
{
	abstract void handleResults();

	@Override
	ResultsHandlingDAO toResultsHandling()
	{
		return this;
	}
}
