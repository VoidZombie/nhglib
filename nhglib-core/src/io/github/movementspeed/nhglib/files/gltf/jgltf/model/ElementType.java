/*
 * www.javagl.de - JglTF
 *
 * Copyright 2015-2017 Marco Hutter - http://www.javagl.de
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.movementspeed.nhglib.files.gltf.jgltf.model;

/**
 * Enumeration of the possible types of elements of an accessor
 */
public enum ElementType
{
    /**
     * The scalar type
     */
    SCALAR(1),
    
    /**
     * The 2D vector type
     */
    VEC2(2),
    
    /**
     * The 3D vector type
     */
    VEC3(3),
    
    /**
     * The 4D vector type
     */
    VEC4(4),
    
    /**
     * The 2x2 matrix type
     */
    MAT2(4),
    
    /**
     * The 3x3 matrix type
     */
    MAT3(9),
    
    /**
     * The 4x4 matrix type
     */
    MAT4(16);
    
    /**
     * The number of components that one element consists of
     */
    private final int numComponents;
    
    /**
     * Creates a new instance with the given number of components
     * 
     * @param numComponents The number of components
     */
    ElementType(int numComponents)
    {
        this.numComponents = numComponents;
    }
    
    /**
     * Returns the number of components that one element consists of
     * 
     * @return The number of components
     */
    public int getNumComponents()
    {
        return numComponents;
    }
    
    /**
     * Returns whether the given string is a valid element type name, and may be
     * passed to <code>ElementType.valueOf</code> without causing an exception.
     * 
     * @param s The string
     * @return Whether the given string is a valid element type
     */
    public static boolean contains(String s)
    {
        for (ElementType elementType : values())
        {
            if (elementType.name().equals(s))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the element type for the given string. If the string is 
     * <code>null</code> or does not describe a valid element type,
     * then <code>null</code> is returned
     * 
     * @param string The string
     * @return The element type
     */
    public static ElementType forString(String string)
    {
        if (string == null)
        {
            return null;
        }
        if (!contains(string))
        {
            return null;
        }
        return ElementType.valueOf(string);
    }
    
}
