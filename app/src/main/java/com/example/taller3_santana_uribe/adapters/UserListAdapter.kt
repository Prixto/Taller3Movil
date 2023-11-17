import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.taller3_santana_uribe.R
import com.example.taller3_santana_uribe.UserLocationActivity
import com.example.taller3_santana_uribe.model.User
import com.squareup.picasso.Picasso

class UserListAdapter(
    private val context: Context,
    private val users: List<User>
) : BaseAdapter() {
    override fun getCount(): Int {
        return users.size
    }

    override fun getItem(position: Int): Any {
        return users[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val user = getItem(position) as User

        val view = LayoutInflater.from(context).inflate(R.layout.user_list_item, null)

        val userNameTextView = view.findViewById<TextView>(R.id.userName)
        userNameTextView.text = "${user.nombre} ${user.apellido}"

        val userImageView = view.findViewById<ImageView>(R.id.userImage)
        Picasso.get()
            .load(user.imageUrl)
            .into(userImageView)
        val viewLocationButton = view.findViewById<Button>(R.id.buttonViewLocation)
        viewLocationButton.setOnClickListener {
            var selectedUserUid = user.email
            val intent = Intent(context, UserLocationActivity::class.java)
            intent.putExtra("selectedUserEmail", selectedUserUid)
            context.startActivity(intent)
        }

        return view
    }
}



