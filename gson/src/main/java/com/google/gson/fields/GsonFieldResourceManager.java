/*
 * Copyright (C) 2019 Becoming Machinic Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.fields;

import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class gives an easy option to cleanup native resources associated with BlobField and ClobField objects. This class is typically utilized via a
 * try block. When the AutoCloseable.close is run any resources registered with the manager will be destroyed.
 * 
 * <pre>
 * try (GsonFieldResourceManager manager = GsonFieldResourceManager.create()) {
 *   Myclass myClass = gson.fromJson(json, Myclass.class);
 *   // use myClass here
 * }
 * </pre>
 * 
 * Creating a new GsonFieldResourceManager will not affect the state of an existing instances.
 * 
 * <pre>
 * try (GsonFieldResourceManager manager1 = GsonFieldResourceManager.create()) {
 *   Myclass myClass = gson.fromJson(json, Myclass.class);
 *   // use myClass here
 *   try (GsonFieldResourceManager manager2 = GsonFieldResourceManager.create()) {
 *     BlobField blobField = manager2.register(new FileBlobField(myTempFile));
 *     json = gson.getJson(new MyClass(blobField));
 *   } // resources registered with manager2 are destroyed here
 * } // resources registered with manager1 are destroyed here
 * </pre>
 * 
 * This class itself is thread safe, but utilizes ThreadLocal to allow Gson to automatically register resources.
 * <p>
 * 
 * @author Caleb Shingledecker
 *
 */
public final class GsonFieldResourceManager implements AutoCloseable {

  private static ThreadLocal<Stack<GsonFieldResourceManager>> resourceManagerThreadLocal = new ThreadLocal<>();

  /**
   * Create a GsonFieldResourceManager.
   * 
   * @return GsonFieldResourceManager instance
   */
  public static GsonFieldResourceManager create() {
    Stack<GsonFieldResourceManager> stack = resourceManagerThreadLocal.get();
    if (stack == null) {
      stack = new Stack<>();
      resourceManagerThreadLocal.set(stack);
    }
    GsonFieldResourceManager manager = new GsonFieldResourceManager();
    stack.push(manager);
    return manager;
  }

  /**
   * Create or get a GsonFieldResourceManager.
   * 
   * @return GsonFieldResourceManager instance
   */
  public static GsonFieldResourceManager createOrGet() {
    Stack<GsonFieldResourceManager> stack = resourceManagerThreadLocal.get();
    if (stack == null) {
      stack = new Stack<>();
      resourceManagerThreadLocal.set(stack);
    }
    GsonFieldResourceManager manager = stack.peek();
    if (manager == null) {
      manager = new GsonFieldResourceManager();
      stack.push(manager);
    }
    return manager;
  }

  public static void clearAll() {
    Stack<GsonFieldResourceManager> stack = resourceManagerThreadLocal.get();
    if (stack != null) {
      for (GsonFieldResourceManager manager : stack) {
        manager.close();
      }
      resourceManagerThreadLocal.remove();
    }
  }

  static void removeManager(GsonFieldResourceManager manager) {
    Stack<GsonFieldResourceManager> stack = resourceManagerThreadLocal.get();
    if (stack != null) {
      while (stack.remove(manager)) {
      }
    }
  }

  /**
   * Get existing GsonFieldResourceManager instance if one exists.
   * 
   * @return GsonFieldResourceManager instance or null
   */
  public static GsonFieldResourceManager get() {
    Stack<GsonFieldResourceManager> stack = resourceManagerThreadLocal.get();
    if (stack != null && !stack.isEmpty()) {
      return stack.peek();
    }
    return null;
  }

  private final Set<StreamingField> resources = ConcurrentHashMap.newKeySet();

  /**
   * Register a BlobField. This method will return the passed object so it can be used to wrap an object.
   * 
   * @param blobField
   * @return the registered BlobField
   */
  public BlobField register(BlobField blobField) {
    this.resources.add(blobField);
    return blobField;
  }

  /**
   * Register a ClobField. This method will return the passed object so it can be used to wrap an object.
   * 
   * @param clobField
   * @return the registered ClobField
   */
  public ClobField register(ClobField clobField) {
    this.resources.add(clobField);
    return clobField;
  }

  /**
   * Destroy this resource manager and all registered resources.
   */
  @Override
  public void close() {
    for (StreamingField resource : this.resources) {
      resource.close();
    }
    removeManager(this);
  }

}
