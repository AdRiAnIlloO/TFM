package jbui.persistence;

import jbui.JBUI;

public abstract class DAO
{
	abstract void executeIO() throws Exception;

	public void makeAsync()
	{
		JBUI.getMaudeThinker().mDAOThread.addDAO(this);
	}

	boolean shouldReplaceThisInList(DAO other)
	{
		return false;
	}

	ResultsHandlingDAO toResultsHandling()
	{
		return null;
	}
}
