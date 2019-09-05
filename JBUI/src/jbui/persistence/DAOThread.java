package jbui.persistence;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.concurrent.Task;

public class DAOThread extends Task<Void>
{
	private List<DAO> mIOExecutableDAOList; // IO pending
	private Lock mIOExecutableDAOListLock;
	private List<ResultsHandlingDAO> mResultsHandleableDAOList; // IO done, ready to handle results
	private Lock mResultsHandleableDAOListLock;

	public DAOThread()
	{
		mIOExecutableDAOList = new LinkedList<>();
		mResultsHandleableDAOList = new LinkedList<>();
		mIOExecutableDAOListLock = new ReentrantLock();
		mResultsHandleableDAOListLock = new ReentrantLock();
	}

	void addDAO(DAO dao)
	{
		mIOExecutableDAOListLock.lock();
		int lastIndex = mIOExecutableDAOList.size();
		mIOExecutableDAOList.add(dao);

		for (int i = lastIndex - 1; i >= 0; i--)
		{
			if (dao.replace(mIOExecutableDAOList.get(i)))
			{
				mIOExecutableDAOList.remove(i);
				break;
			}
		}

		mIOExecutableDAOListLock.unlock();
	}

	@Override
	protected Void call()
	{
		for (; !isCancelled();)
		{
			mIOExecutableDAOListLock.lock();

			if (mIOExecutableDAOList.isEmpty())
			{
				mIOExecutableDAOListLock.unlock();
				continue;
			}

			DAO dao = mIOExecutableDAOList.remove(0);
			mIOExecutableDAOListLock.unlock();

			try
			{
				dao.executeIO();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			ResultsHandlingDAO resultsHandlingDAO = dao.toResultsHandling();

			if (resultsHandlingDAO != null)
			{
				mResultsHandleableDAOListLock.lock();
				mResultsHandleableDAOList.add(resultsHandlingDAO);
				mResultsHandleableDAOListLock.unlock();
			}
		}

		return null;
	}

	public void think()
	{
		mResultsHandleableDAOListLock.lock();
		for (; !mResultsHandleableDAOList.isEmpty(); mResultsHandleableDAOList.remove(0).handleResults());
		mResultsHandleableDAOListLock.unlock();
	}
}
