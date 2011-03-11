﻿/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.rework.joss.persistence.convention.type;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.SQLException;

import com.rework.joss.persistence.convention.extensions.ParameterSetter;
import com.rework.joss.persistence.convention.extensions.ResultGetter;
import com.rework.joss.persistence.convention.extensions.TypeHandlerCallback;

public class BlobTypeHandlerCallback implements TypeHandlerCallback {

 public Object getResult(ResultGetter getter) throws SQLException {
   Blob blob = getter.getBlob();
   byte[] returnValue = null;
   if (null != blob) {
     returnValue = blob.getBytes(1, (int) blob.length());
   } else {
     returnValue = null;
   }
   return returnValue;
 }

 public void setParameter(ParameterSetter setter, Object parameter)
throws SQLException {
   if (null != parameter) {
     byte[] bytes = (byte[]) parameter;
     ByteArrayInputStream bis=new ByteArrayInputStream(bytes);
     setter.setBinaryStream(bis,(int)(bytes.length));
   }
 }

 public Object valueOf(String s) {
   return s;
 }

}