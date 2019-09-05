package jbui.model;

import org.json.JSONException;
import org.json.JSONObject;

class IdElem
{
	private final int mNumber; // Format: N (without curly braces)

	IdElem(int number)
	{
		mNumber = number;
	}

	boolean equals(IdElem idElem)
	{
		return (idElem.mNumber == mNumber);
	}

	void outputAsJSONObject(JSONObject jsonId) throws JSONException
	{
		jsonId.put("elem", mNumber);
	}

	@Override
	public String toString()
	{
		return String.valueOf(mNumber);
	}
}
