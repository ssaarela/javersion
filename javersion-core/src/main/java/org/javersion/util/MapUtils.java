package org.javersion.util;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;

public class MapUtils {

	@SuppressWarnings("rawtypes")
	public static final Function TO_MAP_ENTRY = new Function() {
	    @Override
	    public Object apply(Object input) {
	        return (Map.Entry) input;
	    }
	};
	
	@SuppressWarnings("rawtypes")
	public static final Function GET_KEY = new Function() {
	    @Override
	    public Object apply(Object input) {
	        return ((Entry) input).getKey();
	    }
	};
	
	@SuppressWarnings("rawtypes")
	public static final Function GET_VALUE = new Function() {
	    @Override
	    public Object apply(Object input) {
	        return ((Entry) input).getValue();
	    }
	};

}
