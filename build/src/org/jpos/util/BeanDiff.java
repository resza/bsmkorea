/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2007 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Alcarraz
 * @author Alejandro Revilla
 */
public class BeanDiff {
    /**
     * should update diff info?
     */
    private boolean dirty = false;
    
    /**
     * first bean to compare
     */
    private Object b1;
    
    /**
     * second bean to compare
     */
    private Object b2;
    
    /** 
     * list of object diferences.
     * it has entries for different elements only.
     */
    private List differences;
    
    /**
     * properties to check, if null all properties will get compared
     */
    private String[] properties;
    
    private static Map classInfo = new Hashtable();
    
    private String[] actualProperties;
    
    public BeanDiff () {
        super();
    }

    public BeanDiff (Object b1, Object b2) {
        super();
        setB1 (b1);
        setB2 (b2);
    }
    public BeanDiff (Object b1, Object b2, String[] props) {
        this (b1, b2);
        setProperties (props);
    }
    protected boolean isDirty() {
        return dirty;
    }
    protected void setDirty(boolean dirty) {
        this.dirty = dirty;
        if (dirty) 
            setDifferences (null);
    }

    public Object getB1() {
        return b1;
    }

    public void setB1(Object b1) {
        setDirty (true);
        this.b1 = b1;
    }

    public Object getB2() {
        return b2;
    }

    public void setB2(Object b2) {
        setDirty (true);
        this.b2 = b2;
    }
    
    protected static ClassInfo getClassInfo (String className) 
        throws IntrospectionException
    {
        try {
            return getClassInfo(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return null;
            //FIXME see what to do here
            //throw new DiffException("Managed class does not exists", e);
        }
    }
    
    protected static ClassInfo getClassInfo(Class c) 
        throws IntrospectionException
    {
        ClassInfo ret = (ClassInfo)classInfo.get(c.getName());
        if(ret == null) 
            classInfo.put(c.getName(), ret = new ClassInfo(c));
        return ret;
    }
    
    protected static class ClassInfo {
        protected Class c;
        protected BeanInfo info;
        protected Map properties = new Hashtable();
        public ClassInfo(String className) 
            throws ClassNotFoundException, IntrospectionException
        {
            try {
                c = Class.forName(className);
                init();
            } catch (ClassNotFoundException e){
                // This should never happen as instances of this class
                // should already exist in the current classloader.
            }
        }
        public ClassInfo (Class c) throws IntrospectionException {
            this.c = c;
            init();
        }
        protected final void init() throws IntrospectionException {
            info = Introspector.getBeanInfo(c);
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
            for (int i=0; i<descriptors.length; i++){
                Method reader = descriptors[i].getReadMethod();
                // only add readable attributes, to avoid future errors
                if (reader != null && Modifier.isPublic(reader.getModifiers()) 
                    && !"class".equals(descriptors[i].getName()))
                        properties.put (descriptors[i].getName(), descriptors[i]);
            }
        }
        protected PropertyDescriptor getPropertyDescriptor(String propName){
            return (PropertyDescriptor) properties.get(propName);
        }
        /** 
         * @return property's info.
         */
        public BeanInfo getInfo() {
            return info;
        }
        
        /** 
         * @return property's properties.
         */
        public Map getProperties() {
            return properties;
        }
        
        /**
         * @return Subset of desired properties in a Map whose iteration order
         * is the same as the <code>props</code> array.
         * @param props desired properties
         */
        public Map getProperties(String[] props){
            if (props == null)
                return getProperties();
            Map ret = new LinkedHashMap();
            for(int i = 0; i < props.length; i++){
                ret.put(props[i], properties.get(props[i]));
            }
            return ret;
        }
        
        public Object getValue(Object bean, String prop) {
            try{
                PropertyDescriptor descriptor = getPropertyDescriptor(prop);
                return descriptor.getReadMethod().invoke(bean, null);
            } catch (InvocationTargetException e){
            } catch (IllegalAccessException e){
            }
            return "{error}";
        }
    }
    /**
     * Hook method to let a subclass change the way the values are compared
     * @param i index of the property being compared in de order
     * given by the <code>properties<code> attribute.
     */
    protected boolean equals (int i, Object v1, Object v2){
        if (v1 instanceof Comparable)
            return ((Comparable)v1).compareTo(v2) == 0;
        else if (v1 != null)
            return v1.equals(v2);
        else
            return v2 == null;
    }
    /**
     * intersects two collections the first parameter will hold the 
     * result.
     */
    protected static void intersect (Collection col1, Collection col2){
        Collection col1_col2 = new LinkedList (col1); //clone col1
        col1_col2.removeAll (col2); //
        col1.removeAll (col1_col2); //remve all elements of col1 that are not in col2
    }
    protected void updateActualProperties(ClassInfo info1, ClassInfo info2){
        String[] actualProperties = getProperties();
        List res;
        if (actualProperties != null) {
            res = new LinkedList(Arrays.asList(actualProperties));
            intersect(res, new LinkedList(info1.getProperties().keySet()));
        } else {
            res = new LinkedList(info1.getProperties().keySet());
        }
        intersect(res, info2.getProperties().keySet());
        setActualProperties((String [])res.toArray(new String[] {}));
    }
    protected void updateDiffInfo()
        throws IntrospectionException
    {
        if (!isDirty()) 
            return;
        setDirty(false);
        List differences = new ArrayList();
        setDifferences(differences);
        Object b1 = getB1(), b2 = getB2();
        ClassInfo bean1Props = getClassInfo(b1.getClass());
        ClassInfo bean2Props = getClassInfo(b2.getClass());
        updateActualProperties(bean1Props, bean2Props);
        String[] props = getActualProperties();
        
        for (int i = 0; i < props.length; i++){
            Object v1 = bean1Props.getValue(b1, props[i]);
            Object v2 = bean2Props.getValue(b2, props[i]);
            if (!equals(i, v1, v2))
                differences.add(new DiffEntry(this, props[i], v1, v2));
        }
    }
    public List getDifferences() 
        throws IntrospectionException
    {
        updateDiffInfo();
        return differences;
    }
    protected void setDifferences(List differences) {
        this.differences = differences;
    }
    
    public String toString() {
        try {
            updateDiffInfo();
            Iterator iter = getDifferences().iterator();
            StringBuffer sb = new StringBuffer();
            while (iter.hasNext()) {
                DiffEntry de = (DiffEntry) iter.next();
                sb.append (de.toString());
            }
            return sb.toString();
        } catch (IntrospectionException e) {
            return e.getMessage();
        }
    }
    
    /**
     * @return properties that are effectively being checked, these are the intersection
     * of <code> properties</code>, properties of bean 1 and properties of bean 2
     */
    public String[] getActualProperties()
        throws IntrospectionException
    {
        updateDiffInfo();
        return actualProperties;
    }
    protected void setActualProperties(String[] actualProperties){
        this.actualProperties = actualProperties;
    }
    public String[] getProperties() {
        return properties;
    }
    public void setProperties(String[] properties) {
        setDirty(true);
        this.properties = properties;
    }

}
