/**
 * Provides interfaces and implementations for serializing and deserializing
 * {@link com.swe.canvas.datamodel.action.Action} objects.
 *
 * <p>This package abstracts the serialization mechanism, allowing the
 * underlying format (e.g., JSON, Java Serializable) to be changed.
 * This implementation uses Java's built-in {@link java.io.Serializable}
 * interface for a self-contained, library-free solution.
 * </p>
 *
 * <p>It includes:
 * <ul>
 * <li>{@link com.swe.canvas.datamodel.serialization.SerializedAction}: A DTO
 * wrapper for the serialized data (a byte array).</li>
 * <li>{@link com.swe.canvas.datamodel.serialization.ActionSerializer}: Interface
 * for serializing an {@code Action} to {@code SerializedAction}.</li>
 * <li>{@link com.swe.canvas.datamodel.serialization.ActionDeserializer}: Interface
 * for deserializing a {@code SerializedAction} back into an {@code Action}.</li>
 * <li>Default implementations of the interfaces.</li>
 * </ul>
 * </p>
 *
 * @author Gajjala Bhavani Shankar
 
 
 */
package com.swe.canvas.datamodel.serialization;