/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.ybo.transportsbordeaux.activity.bus;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import fr.ybo.transportsbordeaux.R;
import fr.ybo.transportsbordeaux.activity.commun.MenuAccueil;
import fr.ybo.transportsbordeaux.adapters.bus.DetailTrajetAdapter;
import fr.ybo.transportsbordeaux.application.TransportsBordeauxApplication;
import fr.ybo.transportsbordeaux.database.modele.Direction;
import fr.ybo.transportsbordeaux.database.modele.Ligne;
import fr.ybo.transportsbordeaux.database.modele.Trajet;
import fr.ybo.transportsbordeaux.util.IconeLigne;

import java.util.ArrayList;
import java.util.List;

/**
 * Activitée permettant d'afficher le détail d'un trajet
 *
 * @author ybonnel
 */
public class DetailTrajet extends MenuAccueil.ListActivity {

    private Cursor currentCursor;

    private Trajet trajet;
    private Direction direction;
    private Ligne ligne;
    private int sequence;

    private void recuperationDonneesIntent() {
        trajet = new Trajet();
        trajet.id = getIntent().getExtras().getInt("trajetId");
        sequence = getIntent().getExtras().getInt("sequence");
        trajet = TransportsBordeauxApplication.getDataBaseHelper().selectSingle(trajet);
        direction = new Direction();
        direction.id = trajet.directionId;
        direction = TransportsBordeauxApplication.getDataBaseHelper().selectSingle(direction);
        ligne = new Ligne();
        ligne.id = trajet.ligneId;
        ligne = TransportsBordeauxApplication.getDataBaseHelper().selectSingle(ligne);
    }

    private void gestionViewsTitle() {
        ((TextView) findViewById(R.id.nomLong)).setText(ligne.nomLong);
        ((ImageView) findViewById(R.id.iconeLigne)).setImageResource(IconeLigne.getIconeResource(ligne.nomCourt));
        ((TextView) findViewById(R.id.detailTrajet_nomTrajet)).setText(getString(R.string.vers) + ' ' + direction.direction);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailtrajet);
        recuperationDonneesIntent();
        gestionViewsTitle();
        construireListe();
        ListView lv = getListView();
        lv.setFastScrollEnabled(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Adapter arretAdapter = adapterView.getAdapter();
                Cursor cursor = (Cursor) arretAdapter.getItem(position);
                Intent intent = new Intent(DetailTrajet.this, DetailArret.class);
                intent.putExtra("idArret", cursor.getString(cursor.getColumnIndex("_id")));
                intent.putExtra("nomArret", cursor.getString(cursor.getColumnIndex("nom")));
                intent.putExtra("direction", direction.direction);
                intent.putExtra("ligne", ligne);
                startActivity(intent);
            }
        });
        lv.setTextFilterEnabled(true);
        // Look up the AdView as a resource and load a request.
        ((AdView) this.findViewById(R.id.adView)).loadAd(new AdRequest());
    }

    private void construireListe() {
        StringBuilder requete = new StringBuilder();
        requete.append("SELECT Arret.id as _id, Horaire.heureDepart as heureDepart, Arret.nom as nom ");
        requete.append("FROM Arret, Horaire_");
        requete.append(ligne.id);
        requete.append(" as Horaire ");
        requete.append("WHERE Arret.id = Horaire.arretId");
        requete.append(" AND Horaire.trajetId = :trajetId");
        requete.append(" AND Horaire.stopSequence > :sequence ");
        requete.append("ORDER BY stopSequence;");
        List<String> selectionArgs = new ArrayList<String>(2);
        selectionArgs.add(String.valueOf(trajet.id));
        selectionArgs.add(String.valueOf(sequence));

        currentCursor = TransportsBordeauxApplication.getDataBaseHelper().executeSelectQuery(requete.toString(), selectionArgs);

        setListAdapter(new DetailTrajetAdapter(this, currentCursor));
    }

    private void closeCurrentCursor() {
        if (currentCursor != null && !currentCursor.isClosed()) {
            currentCursor.close();
        }
    }

    @Override
    protected void onDestroy() {
        closeCurrentCursor();
        super.onDestroy();
    }
}