package jbui.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class NonRootIdSystemNode extends IdSystemNode
{
	IdSystemNode mParent;

	NonRootIdSystemNode(IdElem idElem, JSONObject jsonIdSystem) throws JSONException
	{
		super(idElem, jsonIdSystem);
	}

	@Override
	int getDepth()
	{
		return mParent.getDepth() + 1;
	}

	@Override
	public IdSystemNode getParent()
	{
		return mParent;
	}

	@Override
	public void outputIdAsJSONArray(JSONArray jsonArray) throws JSONException
	{
		mParent.outputIdAsJSONArray(jsonArray);
		super.outputIdAsJSONArray(jsonArray);
	}

	@Override
	protected String unparseId(String separator)
	{
		return (mParent.unparseId(separator) + separator + super.unparseId(separator));
	}
}
