package jbui.model;

class IdElem
{
	private int mNumber; // Format: N (without curly braces)
	private int mSubNumber; // Format: {N}. A value of 0 is treated as if sub-element didn't exist.

	IdElem(int number, int subNumber)
	{
		mNumber = number;
		mSubNumber = subNumber;
	}

	boolean equals(IdElem idElem)
	{
		return (idElem.mNumber == mNumber && idElem.mSubNumber == mSubNumber);
	}

	@Override
	public String toString()
	{
		String result = String.valueOf(mNumber);

		if (mSubNumber > 0)
		{
			result += String.format("{%d}", mSubNumber);
		}

		return result;
	}
}
