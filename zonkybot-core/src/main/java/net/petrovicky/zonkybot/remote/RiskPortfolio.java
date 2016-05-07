/*
 * Copyright 2016 Lukáš Petrovický
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.petrovicky.zonkybot.remote;

import javax.xml.bind.annotation.XmlElement;

public class RiskPortfolio {

    private int unpaid, paid, due, totalAmount;
    private String rating;

    @XmlElement
    public int getUnpaid() {
        return unpaid;
    }

    @XmlElement
    public int getPaid() {
        return paid;
    }

    @XmlElement
    public int getDue() {
        return due;
    }

    @XmlElement
    public int getTotalAmount() {
        return totalAmount;
    }

    @XmlElement
    public String getRating() {
        return rating;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RiskPortfolio{");
        sb.append("unpaid=").append(unpaid);
        sb.append(", paid=").append(paid);
        sb.append(", due=").append(due);
        sb.append(", totalAmount=").append(totalAmount);
        sb.append(", rating='").append(rating).append('\'');
        sb.append('}');
        return sb.toString();
    }
}