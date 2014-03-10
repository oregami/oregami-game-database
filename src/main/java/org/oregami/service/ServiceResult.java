package org.oregami.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.oregami.entities.BaseEntity;

/**
 * 
 * @author twendelmuth
 * 
 * @param <T>
 */
public class ServiceResult<T extends BaseEntity> {

    private T result;

    private List<ServiceError> errors;

    public ServiceResult() {
        this(null);
    }

    public ServiceResult(T result) {
        this(result, new ArrayList<ServiceError>());
    }

    public ServiceResult(T result, List<ServiceError> errors) {
        this.result = result;
        this.errors = errors;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public List<ServiceError> getErrors() {
        return errors;
    }

    public void setErrors(List<ServiceError> errors) {
        this.errors = errors;
    }

    public boolean wasSuccessful() {
        return errors != null && errors.size() == 0;
    }

    public boolean hasErrors() {
        return !wasSuccessful();
    }

    public void addMessage(ServiceErrorContext context, ServiceErrorMessage message) {
        errors.add(new ServiceError(context, message));
    }
    
    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
    
//    public boolean containsErrorMessage(ServiceErrorContext serviceErrorContext, ServiceErrorMessage message) {
//    	for (ServiceError error : errors) {
//			if (serviceErrorContext.equals(error.getContext()) && error.getMessageName().equals(message)) {
//				return true;
//			}
//		}
//    	return false;
//    }
    
    public boolean containsError(ServiceError searchError) {
    	for (ServiceError error : errors) {
			if (error.equals(searchError)) {
				return true;
			}
		}
    	return false;
    }    

}
