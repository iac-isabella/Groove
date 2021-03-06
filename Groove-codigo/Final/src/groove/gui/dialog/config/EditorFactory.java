/* GROOVE: GRaphs for Object Oriented VErification
 * Copyright 2003--2011 University of Twente
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * $Id: EditorFactory.java 5512 2014-08-17 11:07:59Z rensink $
 */
package groove.gui.dialog.config;

import groove.explore.config.AcceptorKind;
import groove.explore.config.CheckingKind;
import groove.explore.config.CountKind;
import groove.explore.config.ExploreKey;
import groove.explore.config.MatchKind;
import groove.explore.config.SettingKey;
import groove.gui.dialog.ExploreConfigDialog;

import javax.swing.JPanel;

/**
 * Class that can create editor for a given exploration key,
 * or for a given key/value pair.
 * @author Arend Rensink
 * @version $Revision $
 */
public class EditorFactory {
    /** Constructs a factory for a given dialog. */
    public EditorFactory(ExploreConfigDialog dialog) {
        this.dialog = dialog;
    }

    /** Creates a settings editor for a given exploration key. */
    public SettingEditor createEditor(ExploreKey key) {
        switch (key) {
        case ACCEPTOR:
            return new ButtonEditor(getDialog(), key, "Acceptor");
        case ALGEBRA:
            return new ButtonEditor(getDialog(), key, "Algebra for data values");
        case COUNT:
            return new ButtonEditor(getDialog(), key, "Result count");
        case MATCHER:
            return new ButtonEditor(getDialog(), key, "Match strategy");
        case TRAVERSE:
            return new ButtonEditor(getDialog(), key, "Traversal strategy");
        case CHECKING:
            return new ButtonEditor(getDialog(), key, "Property to check");
        case ISO:
            return new CheckBoxEditor(getDialog(), key, "Isomorphicm checking");
        case RANDOM:
            return new CheckBoxEditor(getDialog(), key, "Randomisation");
        default:
            assert false;
            return null;
        }
    }

    /**
     * Creates a settings editor for a given combination of exploration key and
     * setting kind.
     */
    public SettingEditor createEditor(JPanel holder, ExploreKey key, SettingKey kind) {
        SettingEditor result = null;
        switch (key) {
        case ACCEPTOR:
            if ((AcceptorKind) kind == AcceptorKind.CONDITION) {
                result = new TextFieldEditor(getDialog(), holder, key, kind);
            } else if ((AcceptorKind) kind == AcceptorKind.FORMULA) {
                result = new TextFieldEditor(getDialog(), holder, key, kind);
            }
            break;
        case COUNT:
            if ((CountKind) kind == CountKind.COUNT) {
                result = new TextFieldEditor(getDialog(), holder, key, kind);
            }
            break;
        case MATCHER:
            if ((MatchKind) kind == MatchKind.PLAN) {
                result = new TextFieldEditor(getDialog(), holder, key, kind);
            }
            break;
        case CHECKING:
            if ((CheckingKind) kind == CheckingKind.LTL_CHECK
                || (CheckingKind) kind == CheckingKind.CTL_CHECK) {
                result = new TextFieldEditor(getDialog(), holder, key, kind);
            }
            break;
        case ALGEBRA:
        case ISO:
        case RANDOM:
            // these keys do not have content, hence no holder
            result = new NullEditor(getDialog(), null, key, kind);
        }
        if (result == null) {
            result = new NullEditor(getDialog(), holder, key, kind);
        }
        return result;
    }

    private ExploreConfigDialog getDialog() {
        return this.dialog;
    }

    private final ExploreConfigDialog dialog;
}
