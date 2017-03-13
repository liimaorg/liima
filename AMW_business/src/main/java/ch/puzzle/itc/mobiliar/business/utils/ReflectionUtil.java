/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
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

package ch.puzzle.itc.mobiliar.business.utils;

import org.hibernate.proxy.HibernateProxy;

import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collection;

public final class ReflectionUtil {
    // Ensure non-insatiability.
    private ReflectionUtil() {
    }

    public static <T> Class<T> getActualTypeArguments(Class<?> clazz, int indexOfArgument) {
        Class<T> resolvedType = getActualTypeArguments(clazz.getSuperclass().getGenericSuperclass(), indexOfArgument);
        if (resolvedType == null) {
            return getActualTypeArguments(clazz.getGenericSuperclass(), indexOfArgument);
        }
        return resolvedType;
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getActualTypeArguments(Type type, int indexOfArgument) {
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            Type typeArgument = paramType.getActualTypeArguments()[indexOfArgument];
            if (typeArgument instanceof Class<?>) {
                return (Class<T>) typeArgument;
            }
        }
        return null;
    }

    public static Class<?> getGenericType(Field field) {
        return getGenericType(field, 0);
    }

    public static Class<?> getGenericType(Field field, int typeIndex) {
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        return (Class<?>) type.getActualTypeArguments()[typeIndex];
    }

    public static <T> Object invokeGetter(Field field, Class<T> clazz, T targetObject) throws InvocationTargetException,
            IllegalAccessException, NoSuchMethodException, IntrospectionException {
        PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), clazz);
        if (descriptor.getReadMethod() != null) {
            return descriptor.getReadMethod().invoke(targetObject);
        } else {
            throw new NoSuchMethodException("No getter for " + descriptor.getDisplayName());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void invokeSetter(Field field, Class<T> clazz, T targetObject, Object value) throws IntrospectionException,
            InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), clazz);
        Method setter = descriptor.getWriteMethod();
        if (setter != null) {
            Class<?> targetClass = setter.getParameterTypes()[0];

            // do we need to convert the number type?
            if (value != null && Number.class.isAssignableFrom(targetClass)) {
                Number numValue = (Number) value;
                numValue = convertNumberType(numValue, (Class<? extends Number>) targetClass);
                setter.invoke(targetObject, numValue);
            } else {
                setter.invoke(targetObject, value);
            }
        } else {
            throw new NoSuchMethodException("No setter for " + descriptor.getDisplayName());
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Object[] parameters) throws NoSuchMethodException {
        Class<?>[] parameterClasses = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterClasses[i] = parameters[i] == null ? null : parameters[i].getClass();
        }

        return clazz.getConstructor(parameterClasses);
    }

    public static Number convertNumberType(Number number, Class<? extends Number> targetType) {
        if (Byte.class.isAssignableFrom(targetType)) {
            return number.byteValue();
        } else if (Short.class.isAssignableFrom(targetType)) {
            return number.shortValue();
        } else if (Integer.class.isAssignableFrom(targetType)) {
            return number.intValue();
        } else if (Long.class.isAssignableFrom(targetType)) {
            return number.longValue();
        } else if (Float.class.isAssignableFrom(targetType)) {
            return number.floatValue();
        } else {
            return number.longValue();
        }
    }

    public static <T> T initializeEagerAnnotatedFields(T instance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IntrospectionException {

        Class<?> clazz = instance.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (hasEagerAnnotation(field)) {
                field.setAccessible(true);
                Object object = field.get(instance);
                if (object != null) {
                    if (object instanceof Collection) {
                        Collection collection = (Collection) object;
                        for (Object elem : collection) {
                            initializeEagerAnnotatedFields(elem);
                        }
                    } else {
                        object = unproxyObject(object);
                        if (object != null) {
                            field.set(instance, object);
                            initializeEagerAnnotatedFields(object);
                        }
                    }
                }
            }
        }
        return instance;
    }

    static boolean hasEagerAnnotation(Field field) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (Annotation annotation : field.getAnnotations()) {
            if ((annotation instanceof OneToMany && isEagerFetch(annotation)) || annotation instanceof OneToOne) {
                return true;
            }
        }
        return false;
    }

    static boolean isEagerFetch(Annotation annotation) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method fetch = annotation.getClass().getMethod("fetch");
        FetchType fetchType = (FetchType) fetch.invoke(annotation);
        return FetchType.EAGER.equals(fetchType);
    }

    @SuppressWarnings("unchecked")
    static <T> T unproxyObject(T object) {
        if (object instanceof HibernateProxy) {
            HibernateProxy hibernateProxy = (HibernateProxy) object;
            return (T) hibernateProxy.getHibernateLazyInitializer().getImplementation();
        }
        return null;
    }
}
