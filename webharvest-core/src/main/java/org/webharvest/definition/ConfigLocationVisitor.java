package org.webharvest.definition;

import java.io.IOException;

import org.webharvest.definition.ConfigSource.Location;
import org.webharvest.definition.FileConfigSource.FileLocation;
import org.webharvest.definition.URLConfigSource.URLLocation;

/**
 * Represents object implementing Visitor patter. Provides polymorphic
 * handler methods on visitable location objects that implements
 * {@link VisitableLocation}.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see FileLocation
 * @see URLLocation
 */
interface ConfigLocationVisitor {

    /**
     * Takes certain action on {@link FileLocation}.
     *
     * @param location
     *            reference to {@link VisitableLocation} object.
     * @throws IOException
     *             in case of any problem associated with visitor's action taken
     *             on {@link VisitableLocation} object.
     */
    void visit(FileLocation location) throws IOException;

    /**
     * Takes certain action on {@link URLLocation}.
     *
     * @param location
     *            reference to {@link VisitableLocation} object.
     * @throws IOException
     *             in case of any problem associated with visitor's action taken
     *             on {@link VisitableLocation} object.
     */
    void visit(URLLocation location) throws IOException;

    /**
     * Extended version of {@link Location} interface that closely cooperate
     * with {@link ConfigLocationVisitor} in order to take special action on
     * certain {@link Location} instance of which we haev lost a type.
     *
     * @author Robert Bala
     * @since 2.1.0-SNAPSHOT
     * @version %I%, %G%
     * @see Location
     */
    interface VisitableLocation extends Location {

        /**
         * Accepts {@link ConfigLocationVisitor}
         *
         * @param visitor
         *            reference of {@link ConfigLocationVisitor} to visit.
         * @throws IOException
         *             in case of any problem associated with visitor's action
         *             taken on {@link VisitableLocation} object.
         */
        void accept(ConfigLocationVisitor visitor) throws IOException;

    }

}
