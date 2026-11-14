package com.nexvary.utilitycalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLayoutDirection
import java.util.Locale

private val Black = Color(0xFF05090D)
private val Panel = Color(0xFF101C28)
private val Cyan = Color(0xFF25D9FF)
private val Green = Color(0xFF20F47B)
private val Gold = Color(0xFFFFC400)
private val White = Color(0xFFF3F7FF)
private data class Utility(val title:String,val subtitle:String,val icon:ImageVector,val color:Color)
private val utilities = listOf(
    Utility("الكهرباء","الأجهزة والشرائح والفواتير والتوفير",Icons.Default.Bolt,Gold),
    Utility("الغاز","الغاز الطبيعي والأسطوانات والسخانات",Icons.Default.LocalFireDepartment,Color(0xFFFF762C)),
    Utility("المياه","العدادات والاستهلاك والخزانات",Icons.Default.WaterDrop,Color(0xFF22A8FF)),
    Utility("الطاقة الشمسية","الألواح والبطاريات والإنفرتر",Icons.Default.WbSunny,Green),
    Utility("الوقود","البنزين والسولار والمولدات",Icons.Default.LocalGasStation,Color(0xFFFF4A63)),
    Utility("المحاكاة الذكية","مقارنة المصادر والتكلفة والتوفير",Icons.Default.QueryStats,Color(0xFFB46BFF))
)
class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UtilityApp() }
    }
}
@Composable private fun UtilityApp() {
    val context=LocalContext.current
    val prefs=remember { context.getSharedPreferences("tariff",0) }
    var page by rememberSaveable { mutableStateOf("الرئيسية") }
    var tier by rememberSaveable { mutableStateOf(prefs.getString("tier","") ?: "") }
    var price by rememberSaveable { mutableStateOf(prefs.getString("price","") ?: "") }
    var tariffName by rememberSaveable { mutableStateOf(prefs.getString("name","") ?: "") }
    var watts by rememberSaveable { mutableStateOf("1000") }
    var duration by rememberSaveable { mutableStateOf("1") }
    var saved by remember { mutableStateOf(false) }
    val energy=(watts.toDoubleOrNull() ?: 0.0)*(duration.toDoubleOrNull() ?: 0.0)/1000.0
    val result=price.toDoubleOrNull()?.takeIf { it>=0 }?.let { it*energy }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme=darkColorScheme(background=Black,surface=Panel,primary=Cyan,onSurface=White)) {
            Column(Modifier.fillMaxSize().background(Black)) {
                if(page=="الرئيسية") {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                            IconButton(onClick={page="الإعدادات"}) { Icon(Icons.Default.Settings,"الإعدادات",tint=White) }
                            IconButton(onClick={page="التقارير"}) { Icon(Icons.Default.Notifications,"التنبيهات",tint=White) }
                        }
                        Text("ϟ NEXVARY",Modifier.fillMaxWidth(),fontSize=35.sp,fontWeight=FontWeight.Black,color=Color(0xFFC9D6E5),textAlign=TextAlign.Center)
                        Text("UtilityCalc",Modifier.fillMaxWidth(),fontSize=26.sp,fontWeight=FontWeight.Bold,color=Cyan,textAlign=TextAlign.Center)
                        Text("Smart Utilities & Energy Management",Modifier.fillMaxWidth(),fontSize=12.sp,color=White,textAlign=TextAlign.Center)
                        Spacer(Modifier.height(22.dp))
                        utilities.chunked(3).forEach { row ->
                            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                row.forEach { u ->
                                    Column(Modifier.weight(1f).height(160.dp).border(1.dp,u.color,RoundedCornerShape(18.dp)).background(Brush.verticalGradient(listOf(Panel,Black)),RoundedCornerShape(18.dp)).clickable { page=if(u.title=="الكهرباء") "الكهرباء" else u.title }.padding(6.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                                        Icon(u.icon,null,tint=u.color,modifier=Modifier.size(40.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text(u.title,fontSize=15.sp,fontWeight=FontWeight.Bold,color=White,textAlign=TextAlign.Center)
                                        Text(u.subtitle,fontSize=10.sp,lineHeight=14.sp,color=White,textAlign=TextAlign.Center,maxLines=3)
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                        NeonPanel("ملخص التكاليف الشهري") {
                            Text("لا توجد بيانات استهلاك مسجلة بعد",color=White,fontSize=14.sp)
                            Text("تظهر التكاليف الفعلية بعد إضافة الأجهزة والقراءات",color=Cyan,fontSize=12.sp)
                        }
                    }
                } else if(page=="الكهرباء" || page=="التعريفة") {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
                        Row(verticalAlignment=Alignment.CenterVertically) {
                            IconButton(onClick={page="الرئيسية"}) { Icon(Icons.Default.ArrowForward,null,tint=White) }
                            Text("⚡ الكهرباء",fontSize=26.sp,fontWeight=FontWeight.Bold,color=White)
                        }
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                            listOf("الأجهزة","الاستهلاك","الفواتير","التعريفة","التوفير").forEach { tab ->
                                Box(Modifier.weight(1f).border(1.dp,if(tab=="التعريفة") Gold else Color.DarkGray,RoundedCornerShape(12.dp)).clickable { page="التعريفة" }.padding(vertical=13.dp),contentAlignment=Alignment.Center) {
                                    Text(tab,fontSize=11.sp,color=if(tab=="التعريفة") Gold else White,maxLines=1)
                                }
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        NeonPanel("⚙ إعدادات تعريفة الكهرباء") {
                            Text("أدخل بيانات الشريحة والتكلفة كما تظهر على عدادك",fontSize=13.sp,color=White)
                            Field("رقم الشريحة",tier,{tier=it},"مثال: 2")
                            Field("تكلفة الكيلووات ساعة (بالجنيه)",price,{price=it},"أدخل السعر من العداد",true)
                            Field("اسم التعريفة (اختياري)",tariffName,{tariffName=it},"العداد الرئيسي")
                            Button(onClick={
                                if(tier.toIntOrNull()!=null && price.toDoubleOrNull()?.let{it>=0}==true) {
                                    prefs.edit().putString("tier",tier).putString("price",price).putString("name",tariffName).apply()
                                    saved=true
                                }
                            },enabled=tier.toIntOrNull()!=null && price.toDoubleOrNull()?.let{it>=0}==true,colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF007B43)),modifier=Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Save,null); Spacer(Modifier.width(8.dp)); Text("حفظ التعريفة")
                            }
                            if(saved) Text("تم حفظ التعريفة محليًا",color=Green)
                        }
                        Spacer(Modifier.height(14.dp))
                        NeonPanel("▣ تجربة حساب استهلاك جهاز") {
                            Field("قدرة الجهاز (وات)",watts,{watts=it},"1000",true)
                            Field("مدة التشغيل (ساعة)",duration,{duration=it},"1",true)
                            Box(Modifier.fillMaxWidth().background(Color(0xFF063A32),RoundedCornerShape(14.dp)).padding(16.dp),contentAlignment=Alignment.Center) {
                                Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                    Text("التكلفة التقديرية",color=White)
                                    Text(if(result!=null && watts.toDoubleOrNull()?.let{it>=0}==true && duration.toDoubleOrNull()?.let{it>=0}==true) String.format(Locale.US,"%.2f جنيه",result) else "أدخل تعريفة صحيحة",fontSize=26.sp,fontWeight=FontWeight.Bold,color=Gold)
                                }
                            }
                            Text("الحساب يعتمد على السعر الذي أدخلته، دون إضافة رسوم تلقائية.",fontSize=11.sp,color=Cyan)
                        }
                    }
                } else {
                    Column(Modifier.weight(1f).padding(20.dp)) {
                        IconButton(onClick={page="الرئيسية"}) { Icon(Icons.Default.ArrowForward,null,tint=White) }
                        Text(page,fontSize=26.sp,fontWeight=FontWeight.Bold,color=White)
                        Spacer(Modifier.height(12.dp))
                        UtilityCalculator(page)
                        if(page=="الإعدادات") Button(onClick={page="الكهرباء"}) { Text("إعداد تعريفة الكهرباء") }
                    }
                }
                Row(Modifier.fillMaxWidth().background(Panel).padding(vertical=10.dp),horizontalArrangement=Arrangement.SpaceEvenly) {
                    listOf(Triple("الرئيسية",Icons.Default.Home,"الرئيسية"),Triple("الحاسبات",Icons.Default.Calculate,"الكهرباء"),Triple("الأجهزة",Icons.Default.GridView,"الأجهزة"),Triple("التقارير",Icons.Default.BarChart,"التقارير"),Triple("الأدوات",Icons.Default.Build,"الأدوات")).forEach { (label,icon,dest) ->
                        Column(Modifier.clickable{page=dest}.weight(1f),horizontalAlignment=Alignment.CenterHorizontally) {
                            Icon(icon,null,tint=if(page==dest) Cyan else White)
                            Text(label,fontSize=11.sp,color=if(page==dest) Cyan else White)
                        }
                    }
                }
            }
        }
    }
}
@Composable private fun NeonPanel(title:String,content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxWidth().border(1.dp,Cyan,RoundedCornerShape(18.dp)).background(Brush.verticalGradient(listOf(Color(0xFF102B40),Black)),RoundedCornerShape(18.dp)).padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text(title,fontSize=20.sp,fontWeight=FontWeight.Bold,color=White)
        content()
    }
}
@Composable private fun Field(label:String,value:String,onValue:(String)->Unit,hint:String,numeric:Boolean=false) {
    Column {
        Text(label,color=White,fontSize=13.sp)
        OutlinedTextField(value=value,onValueChange=onValue,placeholder={Text(hint,color=Color.Gray,fontSize=12.sp)},singleLine=true,modifier=Modifier.fillMaxWidth(),keyboardOptions=KeyboardOptions(keyboardType=if(numeric) KeyboardType.Decimal else KeyboardType.Text),colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=Cyan,unfocusedBorderColor=Color(0xFF6A8497),focusedTextColor=White,unfocusedTextColor=White))
    }
}

@Composable private fun UtilityCalculator(section:String) {
    val context=LocalContext.current
    val prefs=remember { context.getSharedPreferences("utility_prices",0) }
    var quantity by rememberSaveable(section) { mutableStateOf("") }
    var unitPrice by rememberSaveable(section) { mutableStateOf(prefs.getString(section,"") ?: "") }
    var watts by rememberSaveable(section) { mutableStateOf("") }
    var hours by rememberSaveable(section) { mutableStateOf("") }
    var days by rememberSaveable(section) { mutableStateOf("") }
    var batteryVolts by rememberSaveable(section) { mutableStateOf("12") }
    var batteryAh by rememberSaveable(section) { mutableStateOf("70") }
    var efficiency by rememberSaveable(section) { mutableStateOf("85") }
    var message by remember { mutableStateOf("") }
    val unit=when(section) {
        "الغاز" -> "متر مكعب أو كيلوجرام"
        "المياه" -> "متر مكعب"
        "الوقود" -> "لتر"
        else -> "وحدة"
    }
    NeonPanel("حاسبة $section") {
        when(section) {
            "الغاز","المياه","الوقود" -> {
                Field("الكمية المستهلكة ($unit)",quantity,{quantity=it},"أدخل الكمية",true)
                Field("سعر الوحدة بالجنيه",unitPrice,{unitPrice=it},"لا يوجد سعر افتراضي",true)
                val q=quantity.toDoubleOrNull()
                val p=unitPrice.toDoubleOrNull()
                val valid=q!=null && p!=null && q>=0 && p>=0 && q.isFinite() && p.isFinite()
                Button(onClick={prefs.edit().putString(section,unitPrice).apply();message="تم حفظ السعر على الجهاز"},enabled=p!=null && p>=0 && p.isFinite()) { Text("حفظ السعر") }
                if(message.isNotEmpty()) Text(message,color=Green)
                Text(if(valid) "التكلفة: "+String.format(Locale.US,"%.2f جنيه",q!!*p!!) else "أدخل الكمية والسعر لحساب التكلفة",color=Gold,fontSize=20.sp)
                Text("الحساب لا يضيف رسومًا أو شرائح لم تُدخلها.",color=Cyan,fontSize=12.sp)
            }
            "الطاقة الشمسية" -> {
                Text("حاسبة البطارية والإنفرتر",color=Green,fontSize=18.sp)
                Field("جهد البطارية (V)",batteryVolts,{batteryVolts=it},"12",true)
                Field("سعة البطارية (Ah)",batteryAh,{batteryAh=it},"70",true)
                Field("قدرة الجهاز (W)",watts,{watts=it},"1000",true)
                Field("كفاءة الإنفرتر (%)",efficiency,{efficiency=it},"85",true)
                val v=batteryVolts.toDoubleOrNull()
                val a=batteryAh.toDoubleOrNull()
                val w=watts.toDoubleOrNull()
                val e=efficiency.toDoubleOrNull()
                if(v!=null && a!=null && w!=null && e!=null && v>0 && a>0 && w>0 && e>0 && e<=100 && listOf(v,a,w,e).all{it.isFinite()}) {
                    val minutes=v*a*(e/100.0)/w*60.0
                    Text("الحد النظري بعد فاقد الإنفرتر: "+String.format(Locale.US,"%.1f دقيقة",minutes),color=Gold,fontSize=18.sp)
                    Text("هذه ليست مدة تشغيل مضمونة؛ بطاريات الرصاص تفقد سعتها عند التيارات العالية وقد يفصل الإنفرتر مبكرًا. يلزم نوع البطارية ومنحنى التفريغ لحساب أدق.",color=Cyan,fontSize=12.sp)
                } else Text("أدخل بيانات البطارية والجهاز",color=White)
            }
            "المحاكاة الذكية" -> {
                Text("مقارنة تكلفة جهاز يعمل بالكهرباء",color=White)
                Field("قدرة الجهاز (W)",watts,{watts=it},"1000",true)
                Field("ساعات التشغيل يوميًا",hours,{hours=it},"4",true)
                Field("عدد الأيام",days,{days=it},"30",true)
                Field("سعر الكيلووات ساعة بالجنيه",unitPrice,{unitPrice=it},"أدخل السعر",true)
                val values=listOf(watts,hours,days,unitPrice).map{it.toDoubleOrNull()}
                if(values.all{it!=null && it>=0 && it.isFinite()}) {
                    val energy=values[0]!!*values[1]!!*values[2]!!/1000.0
                    Text("الطاقة: "+String.format(Locale.US,"%.2f kWh",energy),color=White)
                    Text("التكلفة: "+String.format(Locale.US,"%.2f جنيه",energy*values[3]!!),color=Gold,fontSize=20.sp)
                } else Text("أدخل القيم للمقارنة",color=Cyan)
            }
            else -> Text("لا توجد بيانات مسجلة بعد",color=Cyan)
        }
    }
}
