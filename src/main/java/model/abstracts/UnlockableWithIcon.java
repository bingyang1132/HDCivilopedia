package model.abstracts;

import model.Civic;
import model.Technology;

public abstract class UnlockableWithIcon extends WritableWithIcon {

    public UnlockableWithIcon (String tag) {
        super(tag);
    }

    public String prereqTech;
    public String prereqCivic;

    @Override
    public int getOrder() {
        if (prereqCivic != null) {
            Civic civic = Civic.civics.get(prereqCivic);
            if (civic != null) {
                return civic.getOrder();
            }
        }
        if (prereqTech != null) {
            Technology technology = Technology.technologies.get(prereqTech);
            if (technology != null) {
                return technology.getOrder();
            }
        }
        return super.getOrder();
    }

}