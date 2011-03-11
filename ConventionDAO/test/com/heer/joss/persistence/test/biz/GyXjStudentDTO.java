/*
 * Copyright (c) 2006 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *  
 * Author Heer InfoTech Co., Ltd.
 *
 * $Id: GyXjStudentDTO.java,v 1.1 2009/07/24 04:33:13 zhujj Exp $
 */

package com.heer.joss.persistence.test.biz;

import com.rework.core.dto.BaseObject;

/**
 * 学生的学籍信息、基本信息和联系方式
 * @author zhujj
 * @since 2006-12-27
 */
public class GyXjStudentDTO extends BaseObject{
	// 
	private String studentId;
	// 姓名
	private String xm;
	// 性别 GB-20001
	private String xb;
	// 性别码
	private String xbm;
	// 学号
	private String xh;
	// 学生类别 --DM-10012
	private String xslb;
	// 学生类别码
	private String xslbm;
	// 校区 HR-30011
	private String xq;
	// 学院名称
	private String xy;
	// 学院id
	private String xyId;
	// 系所名称
	private String xs;
	// 系所id
	private String xsId;
	// 当前所在年级(以四位年份表示，一般指入学时的年份，如果发生留级等情况，则是指留级后所在的年级)
	private String nj;
	// 专业名称（对于本专科生为专业开设名称，对于研究生为专业名称）
	private String zy;
	// 专业号（对于本专科生为专业开设号，对于研究生为专业号）
	private String zyh;
	// 专业科类  --DM-10004
	private String zykl;
	// 专业科类码
	private String zyklm;
	// 专业id（对于本专科生为专业开设id，对于研究生为专业的id）
	private String zyId;
	// 班号
	private String bh;
	// 班级名称
	private String bjmc;
	// 学制
	private String xz;
	// 培养层次 DD-10013
	private String pycc;
	
	private MessageDTO message = new MessageDTO();
	
	public String getStudentId() {
		return studentId;
	}
	public MessageDTO getMessage() {
		return message;
	}
	public void setMessage(MessageDTO message) {
		this.message = message;
	}
	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}
	public String getXm() {
		return xm;
	}
	public void setXm(String xm) {
		this.xm = xm;
	}
	public String getXb() {
		return xb;
	}
	public void setXb(String xb) {
		this.xb = xb;
	}
	public String getXbm() {
		return xbm;
	}
	public void setXbm(String xbm) {
		this.xbm = xbm;
	}
	public String getXh() {
		return xh;
	}
	public void setXh(String xh) {
		this.xh = xh;
	}
	public String getXslb() {
		return xslb;
	}
	public void setXslb(String xslb) {
		this.xslb = xslb;
	}
	public String getXslbm() {
		return xslbm;
	}
	public void setXslbm(String xslbm) {
		this.xslbm = xslbm;
	}
	public String getXq() {
		return xq;
	}
	public void setXq(String xq) {
		this.xq = xq;
	}
	public String getXy() {
		return xy;
	}
	public void setXy(String xy) {
		this.xy = xy;
	}
	public String getXyId() {
		return xyId;
	}
	public void setXyId(String xyId) {
		this.xyId = xyId;
	}
	public String getXs() {
		return xs;
	}
	public void setXs(String xs) {
		this.xs = xs;
	}
	public String getXsId() {
		return xsId;
	}
	public void setXsId(String xsId) {
		this.xsId = xsId;
	}
	public String getNj() {
		return nj;
	}
	public void setNj(String nj) {
		this.nj = nj;
	}
	public String getZy() {
		return zy;
	}
	public void setZy(String zy) {
		this.zy = zy;
	}
	public String getZyh() {
		return zyh;
	}
	public void setZyh(String zyh) {
		this.zyh = zyh;
	}
	public String getZykl() {
		return zykl;
	}
	public void setZykl(String zykl) {
		this.zykl = zykl;
	}
	public String getZyklm() {
		return zyklm;
	}
	public void setZyklm(String zyklm) {
		this.zyklm = zyklm;
	}
	public String getZyId() {
		return zyId;
	}
	public void setZyId(String zyId) {
		this.zyId = zyId;
	}
	public String getBh() {
		return bh;
	}
	public void setBh(String bh) {
		this.bh = bh;
	}
	public String getBjmc() {
		return bjmc;
	}
	public void setBjmc(String bjmc) {
		this.bjmc = bjmc;
	}
	public String getXz() {
		return xz;
	}
	public void setXz(String xz) {
		this.xz = xz;
	}
	public String getPycc() {
		return pycc;
	}
	public void setPycc(String pycc) {
		this.pycc = pycc;
	}
	
}
