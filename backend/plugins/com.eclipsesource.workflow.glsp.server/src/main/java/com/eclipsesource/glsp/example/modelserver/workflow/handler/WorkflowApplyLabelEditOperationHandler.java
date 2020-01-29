/*******************************************************************************
 * Copyright (c) 2019 EclipseSource and others.
 *  
 *   This program and the accompanying materials are made available under the
 *   terms of the Eclipse Public License v. 2.0 which is available at
 *   http://www.eclipse.org/legal/epl-2.0.
 *  
 *   This Source Code may also be made available under the following Secondary
 *   Licenses when the conditions for such availability set forth in the Eclipse
 *   Public License v. 2.0 are satisfied: GNU General Public License, version 2
 *   with the GNU Classpath Exception which is available at
 *   https://www.gnu.org/software/classpath/license.html.
 *  
 *   SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ******************************************************************************/
package com.eclipsesource.glsp.example.modelserver.workflow.handler;

import java.util.Optional;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;

import org.eclipse.glsp.api.action.Action;
import org.eclipse.glsp.api.action.kind.AbstractOperationAction;
import org.eclipse.glsp.api.action.kind.ApplyLabelEditOperationAction;
import org.eclipse.glsp.api.model.GraphicalModelState;
import com.eclipsesource.glsp.example.modelserver.workflow.model.WorkflowModelServerAccess;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.emfcloud.modelserver.coffee.model.coffee.CoffeePackage;
import org.eclipse.emfcloud.modelserver.coffee.model.coffee.Node;
import org.eclipse.emfcloud.modelserver.coffee.model.coffee.Task;

public class WorkflowApplyLabelEditOperationHandler implements ModelStateAwareOperationHandler {

	@Override
	public Class<? extends Action> handlesActionType() {
		return ApplyLabelEditOperationAction.class;
	}

	@Override
	public void doExecute(AbstractOperationAction action, GraphicalModelState modelState,
			WorkflowModelServerAccess modelAccess) throws Exception {
		ApplyLabelEditOperationAction editLabelAction = (ApplyLabelEditOperationAction) action;
		Optional<GModelElement> element = modelState.getIndex().get(editLabelAction.getLabelId());
		if (!element.isPresent() && !(element.get() instanceof GLabel)) {
			throw new IllegalArgumentException("Element with provided ID cannot be found or is not a GLabel");
		}

		GNode gNode = getParentGNode((GLabel) element.get());
		Node node = modelAccess.getNodeById(gNode.getId());
		if (!(node instanceof Task)) {
			throw new IllegalAccessError("Edited label isn't label representing a task");
		}
		
		Command setCommand = SetCommand.create(modelAccess.getEditingDomain(), node, CoffeePackage.Literals.TASK__NAME, editLabelAction.getText());
		if(!modelAccess.edit(setCommand).thenApply(res -> res.body()).get()) {
			throw new IllegalAccessError("Could not execute command: " + setCommand);
		}
	}

	public GNode getParentGNode(GLabel sLabel) {
		EObject parent = sLabel;
		while (!(parent instanceof GNode) || parent == null) {
			parent = parent.eContainer();
		}
		if (!(parent instanceof GNode)) {
			throw new IllegalArgumentException("Cannot find node parent of label");
		}
		return (GNode) parent;
	}

	@Override
	public String getLabel(AbstractOperationAction action) {
		return "Apply label";
	}

}