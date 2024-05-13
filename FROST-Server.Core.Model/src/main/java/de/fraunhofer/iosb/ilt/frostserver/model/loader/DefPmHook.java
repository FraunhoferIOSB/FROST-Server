/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.model.loader;

import de.fraunhofer.iosb.ilt.configurable.AnnotatedConfigurable;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorDouble;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorSubclass;

/**
 * Pair of PmHook and Priority for said hook.
 */
public class DefPmHook implements AnnotatedConfigurable<Void, Void> {

    @ConfigurableField(editor = EditorDouble.class, optional = true,
            label = "Priority", description = "Priority of the Hook. Hooks with lower priority are executed earlier.")
    @EditorDouble.EdOptsDouble(max = 100, min = -100, step = 0.1)
    private double priority;

    @ConfigurableField(editor = EditorSubclass.class,
            label = "PM Hooks", description = "Persistence Manager Hooks")
    @EditorSubclass.EdOptsSubclass(iface = PmHook.class, merge = true, nameField = "@class", shortenClassNames = true)
    private PmHook hook;

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public PmHook getHook() {
        return hook;
    }

    public void setHook(PmHook hook) {
        this.hook = hook;
    }

}
