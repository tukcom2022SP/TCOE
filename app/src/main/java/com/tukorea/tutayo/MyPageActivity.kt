package com.tukorea.tutayo

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
<<<<<<< HEAD
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Toast
=======
import android.widget.*
>>>>>>> ac68ca7166958ce4fa7c0951aefcff307cc5f052
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient
import kotlinx.android.synthetic.main.mypage_activity.*
import kotlinx.android.synthetic.main.mypage_request.*
<<<<<<< HEAD
import kotlinx.android.synthetic.main.mypage_share_item.*
=======

>>>>>>> ac68ca7166958ce4fa7c0951aefcff307cc5f052
import kotlinx.android.synthetic.main.taxi_activity.*
import kotlinx.android.synthetic.main.taxi_fragment_jeongwang.*
import kotlinx.android.synthetic.main.taxi_share_dialog.*
import kotlinx.android.synthetic.main.taxi_share_dialog.detail_cancelBtn
import kotlinx.android.synthetic.main.taxi_share_item.view.*
import java.security.AccessController.getContext
import java.text.FieldPosition

class MyPageActivity : AppCompatActivity() {

    lateinit var taxiActivity: TaxiActivity
    private var firestore: FirebaseFirestore? = null
    lateinit var image:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.mypage_activity)

        image = findViewById(R.id.img)

        UserApiClient.instance.me { user, error ->
            id.text = "${user?.id}"
            name.text = "${user?.kakaoAccount?.profile?.nickname}"
            email.text = "${user?.kakaoAccount?.email}"
            gender.text = "${user?.kakaoAccount?.gender}"
            if(gender.text=="FEMALE"){
                image.setImageResource(R.drawable.aunt)
            }
            /*else if(gender.text =="MALE"){
                image.setImageResource(R.drawable.uncle)

            }*/
        }


        firestore = FirebaseFirestore.getInstance()
        var dlg = myDialog(this)
        var myRecyclerAdapter = myRecyclerViewAdapter(this, dlg)

        mygeul.adapter = myRecyclerAdapter
        mygeul.layoutManager = LinearLayoutManager(this)

    }

    inner class myDialog(context: Context){
        private var dialog = Dialog(context)
        private var myReqData : ArrayList<TaxiData> = arrayListOf()


        init{


            dialog.setContentView(R.layout.mypage_request)
            firestore?.collection("jwTaxiShare")?.addSnapshotListener {
                    querySnapshot, firebaseFirestoreException ->
                myReqData.clear()

                if(querySnapshot != null) {
                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(TaxiData::class.java)
                        item!!.docId = snapshot.id
                        myReqData.add(item!!)
                    }

                }
                else Log.i("TAG","querySnapshot : null")
                var sortedJwTaxiData = myReqData.sortedBy { it.uploadTime }

                myReqData = ArrayList(sortedJwTaxiData.reversed())
            }







            dialog.detail_cancelBtn.setOnClickListener {
                dialog.dismiss()
            }
        }


        fun showDialog(){
            dialog.show()
        }

    }

    inner class myRecyclerViewAdapter(context: Context, dlg : myDialog): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        private var mytaxidata : ArrayList<TaxiData> = arrayListOf()
        private var context : Context = context
        private var data : TaxiData? = null
        private var dialog = dlg

        val intent = getIntent()!!
        var userId = intent.getLongExtra("user_id", 0)

        init{
            mytaxidata.clear() //내 글 리스트를 비워줌

            firestore?.collection("jwTaxiShare")?.addSnapshotListener { querySnapShot, firebaseFireStoreException ->

                if(querySnapShot != null){
                    for(snapshot in querySnapShot.documents){
                        var item = snapshot.toObject(TaxiData::class.java)
                        if(item!!.kakaoUserId == userId){
                            mytaxidata.add(item!!)
                        }
                        notifyDataSetChanged()
                    }
                }
                Log.i("TAG", "mypage userID: ${userId}")

            }


            firestore?.collection("oidoTaxiShare")?.addSnapshotListener { querySnapShot, firebaseFireStoreException ->

                if(querySnapShot != null){
                    for(snapshot in querySnapShot.documents){
                        var item = snapshot.toObject(TaxiData::class.java)
                        item!!.docId = snapshot.id
                        if(item!!.kakaoUserId == userId) {
                            mytaxidata.add(item!!)
                        }
                        notifyDataSetChanged()
                    }
                }

            }
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        }

        override fun onCreateViewHolder(parent : ViewGroup, viewType : Int):RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.taxi_share_item, parent, false)
            return ViewHolder(view)


        }

        //onCreateViewHolder에서 만든 뷰와 실제 데이터를 연결한다.
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as ViewHolder).itemView

            var hour = mytaxidata[position].departure_hour.toString().toInt()

            if(hour >= 12) viewHolder.item_ampm.text = "오후"
            else viewHolder.item_ampm.text = "오전"

            //출발 시간 12시간 단위로 변환하여 설정
            if(hour > 12) viewHolder.item_departure_hour.text = (hour - 12).toString()
            else if(hour == 0) viewHolder.item_departure_hour.text = "12"
            else viewHolder.item_departure_hour.text = hour.toString()

            viewHolder.item_departure_minute.text = mytaxidata[position].departure_minute.toString()

            when(mytaxidata[position].position) {
                0 -> viewHolder.item_position.text = "정왕"
                1 -> viewHolder.item_position.text = "오이도"
            }
            viewHolder.item_entrance.text = mytaxidata[position].entranceNum.toString()
            viewHolder.item_current_num.text = mytaxidata[position].shareMember.size.toString()
            viewHolder.item_max_num.text = mytaxidata[position].maxNum.toString()

            if(mytaxidata[position].restriction == 0) {  //성별 제한이 없는 경우
            }
            else if (mytaxidata[position].restriction == 1 && mytaxidata[position].gender == "MALE") { // 동성만 제한하고 작성자가 남자인 경우
                viewHolder.item_female.visibility = View.GONE

            }
            else { // 동성만 제한하고 작성자가 여자인 경우
                viewHolder.item_male.visibility = View.GONE
            }


            //lst1.adapter = shareReqAdapter(context, )

            viewHolder.setOnClickListener() {
              /*  var dlg = myDialog(context)
                dlg.showDialog()*/
                var intent = Intent(context,MYMYActivity::class.java)
                startActivity(intent)
                Handler().postDelayed({ viewHolder.item_current_num.text ="1" }, 2000)

                /*Toast.makeText(this@MyPageActivity, "합승이 허용되었습니다", Toast.LENGTH_SHORT).show()*/
            }

        }

        override fun getItemCount(): Int {
            return mytaxidata.size
        }

//        inner class shareReqAdapter(val context: Context, val taxiList: ArrayList<TaxiData>) : BaseAdapter() {
//            override fun getCount(): Int {
//                TODO("Not yet implemented")
//                return taxiList.size
//            }
//
//            override fun getItem(p0: Int): Any {
//                TODO("Not yet implemented")
//                return taxiList[p0]
//            }
//
//            override fun getItemId(p0: Int): Long {
//                TODO("Not yet implemented")
//                return 0
//            }
//
//            override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
//                TODO("Not yet implemented")
//
//                val view: View = LayoutInflater.from(context).inflate(R.layout.mypage_share_item,null)
//                mypage_item_user_id.text = taxiList[p0].requestUser.toString()
//                if(taxiList[p0].gender == "FEMALE") mypage_item_user_male.visibility = View.GONE
//                else mypage_item_user_female.visibility = View.GONE
//            }
//
//
//        }
    }








}

