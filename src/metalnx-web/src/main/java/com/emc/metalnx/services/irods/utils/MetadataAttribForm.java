package com.emc.metalnx.services.irods.utils;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.*;

//@XmlRootElement(name = "MyResult")
public class MetadataAttribForm {
	/**
	 * offset used to get this result set
	 */
	//@XmlElement(name = "Offset")
	private int offset = 0;
	
	/**
	 * are there more elements to return from the original query?
	 */
	//@XmlElement(name = "More")
	private boolean more = false;
	
	/**
	 * The actual avu attribute or value, as appropriate to the query
	 */
	//@XmlElement(name="Elements")
	private List<String> elements = new ArrayList<String>();
	
	//@XmlElement(name="NextOffset")
	private int nextOffset = 0;

	/**
	 * 
	 */
	public MetadataAttribForm() {
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public boolean isMore() {
		return more;
	}

	public void setMore(boolean more) {
		this.more = more;
	}

	public List<String> getElements() {
		return elements;
	}

	public void setElements(List<String> elements) {
		this.elements = elements;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("AvuSearchResult [offset=").append(offset).append(", more=").append(more).append(", ");
		if (elements != null) {
			builder.append("elements=").append(elements.subList(0, Math.min(elements.size(), maxLen))).append(", ");
		}
		builder.append("nextOffset=").append(nextOffset).append("]");
		return builder.toString();
	}

	/**
	 * @return the nextOffset
	 */
	public int getNextOffset() {
		return nextOffset;
	}

	/**
	 * @param nextOffset
	 *            the nextOffset to set
	 */
	public void setNextOffset(int nextOffset) {
		this.nextOffset = nextOffset;
	}
}

/*
 * AvuSearchResult[offset=0,more=false,elements=[
 * testGatherAvailableAttributesForDataObjWithPrefix-testmdattrib1,attb2,att1,
 * testGatherAvailableValuesForDataObjWithPrefix-testmdattrib1,
 * testProcessValidManifestFailFastRelativePath,kjhsdjfhsdkljfhsdjkfhsdkjalfhsd,
 * foo,thisisnew,testGatherAvailableValuesForDataObjNoPrefix-testmdattrib1,
 * testGatherAvailableAttributesForCollNoPrefix-testmdattrib2],nextOffset=0]
 */