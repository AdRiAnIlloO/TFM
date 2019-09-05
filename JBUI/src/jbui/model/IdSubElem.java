package jbui.model;

import org.json.JSONException;
import org.json.JSONObject;

class IdSubElem extends IdElem
{
	private final int mSubNumber; // Format: {N}

	IdSubElem(int number, int subNumber)
	{
		super(number);
		mSubNumber = subNumber;
	}

	@Override
	boolean equals(IdElem idElem)
	{
		if (idElem instanceof IdSubElem)
		{
			return (super.equals(idElem) && ((IdSubElem) idElem).mSubNumber == mSubNumber);
		}

		return false;
	}

	@Override
	void outputAsJSONObject(JSONObject jsonId) throws JSONException
	{
		super.outputAsJSONObject(jsonId);
		jsonId.put("subElem", mSubNumber);
	}

	@Override
	public String toString()
	{
		return (super.toString() + String.format("{%d}", mSubNumber));
	}
}
