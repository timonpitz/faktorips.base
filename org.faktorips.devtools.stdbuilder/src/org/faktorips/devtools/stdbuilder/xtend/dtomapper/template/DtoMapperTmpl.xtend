/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 
 * 3. 
 *  
 * Please see LICENSE.txt for full license terms, including the additional permissions and 
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xtend.dtomapper.template

import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass

import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import org.faktorips.devtools.stdbuilder.xmodel.dtomapper.XDtoMapper
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType
import org.faktorips.devtools.stdbuilder.xmodel.XAttribute

class DtoMapperTmpl {
	def static String generate(XDtoMapper it)'''
		/**
		* @generated
		*/
		public class «implClassName»{
			
			«variableDeclaration»
			
			«constructors»
			
			«mapperToDto»
			
			«mapperToPolicyModel»
		}
		
	'''
	
	def private static variableDeclaration(XDtoMapper it)'''
		/**
		* @generated
		*/
		private «IRuntimeRepository» runtimeRepository;
	'''
	
	def private static constructors(XDtoMapper it)'''
		/**
		* @generated
		*/
		protected «method(implClassName, IRuntimeRepository, "runtimeRepository")»{
			this.runtimeRepository = runtimeRepository;
		}
	'''
	
	def private static mapperToDto(XDtoMapper it)'''
		/**
		* @generated
		*/
		public DtoPolicy mapToDto(«policyName» it){
			DtoPolicy dtoPolicy = new DtoPolicy();
			
			«FOR attribute : getAttributes»
				«mapAttributesToDto(attribute)»
			«ENDFOR»
			
			return dtoPolicy;
		}
	'''
	
	def private static mapperToPolicyModel(XDtoMapper it)'''
		/**
		* @generated
		*/
		public «policyName» mapToPolicyModel(DtoPolicy it){
			«policyName» policy = new «policyName»();
			
			«FOR attribute : getAttributes»
				«mapAttributesToPolicy(attribute)»
			«ENDFOR»
		}
	'''
	
	def private static mapAttributesToPolicy(XAttribute it)'''
		«IF attribute.isExtPropertyDefinitionAvailable("org.faktorips.devtools.stdbuilder.mapTo")»
			policy.«methodNameSetter»(it.get«(attribute.getExtPropertyValue("org.faktorips.devtools.stdbuilder.mapTo")).toString.toFirstUpper»());
		«ENDIF»
	'''
	
	def private static mapAttributesToDto(XAttribute it)'''
		«IF attribute.isExtPropertyDefinitionAvailable("org.faktorips.devtools.stdbuilder.mapTo")»
			dtoPolicy.set«(attribute.getExtPropertyValue("org.faktorips.devtools.stdbuilder.mapTo")).toString.toFirstUpper»(it.«methodNameGetter»());
		«ENDIF»
	'''
}