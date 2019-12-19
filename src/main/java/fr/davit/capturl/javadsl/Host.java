/*
 * Copyright 2019 Michel Davit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.davit.capturl.javadsl;

import fr.davit.capturl.scaladsl.Host$;

public abstract class Host {

    public static Host create(String address) {
        return Host$.MODULE$.apply(address);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Host
    //------------------------------------------------------------------------------------------------------------------
    public abstract boolean isEmpty();

    public abstract String getAddress();

    public abstract boolean isIPv4();

    public abstract boolean isIPv6();
    public abstract boolean isNamedHost();

    public abstract fr.davit.capturl.scaladsl.Host asScala();
}
