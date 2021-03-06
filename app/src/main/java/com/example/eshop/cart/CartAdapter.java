package com.example.eshop.cart;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.eshop.R;
import com.example.eshop.db.DBQueries;
import com.example.eshop.product.ProductDetailsActivity;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter {

    private List<CartItemModel> cartItemModelList;
    private int lastPosition = -1;
    private TextView cartTotalAmount;
    private boolean showDeleteBtn;

    public CartAdapter(List<CartItemModel> cartItemModelList, TextView cartTotalAmount, boolean showDeleteBtn) {
        this.cartItemModelList = cartItemModelList;
        this.cartTotalAmount = cartTotalAmount;
        this.showDeleteBtn = showDeleteBtn;
    }

    @Override
    public int getItemViewType(int position) {
        switch (cartItemModelList.get(position).getType()) {
            case 0:
                return CartItemModel.CART_ITEM;
            case 1:
                return CartItemModel.TOTAL_AMOUNT;
            default:
                return -1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case CartItemModel.CART_ITEM:
                View cartItemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cart_item_layout, viewGroup, false);
                return new CartItemViewholder(cartItemView);
            case CartItemModel.TOTAL_AMOUNT:
                View cartTotalView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cart_total_amount_layout, viewGroup, false);
                return new CartTotalAmountViewholder(cartTotalView);

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        switch (cartItemModelList.get(position).getType()) {
            case CartItemModel.CART_ITEM:
                String productID = cartItemModelList.get(position).getProductID();
                String resource = cartItemModelList.get(position).getProductImage();
                String title = cartItemModelList.get(position).getProductTitle();
                Long freeCoupons = cartItemModelList.get(position).getFreeCoupons();
                String productPrice = cartItemModelList.get(position).getProductPrice();
                String cuttedPrice = cartItemModelList.get(position).getCuttedPrice();
                Long offersApplied = cartItemModelList.get(position).getOffersApplied();
                boolean inStock = cartItemModelList.get(position).isInStock();
                ((CartItemViewholder) viewHolder).setItemDetails(productID, resource, title, freeCoupons, productPrice, cuttedPrice, offersApplied, position, inStock);
                break;
            case CartItemModel.TOTAL_AMOUNT:
                int totalItems = 0;
                int totalItemPrice = 0;
                String deliveryPrice;
                int totalAmount;
                int savedAmount = 0;

                for (int x = 0; x < cartItemModelList.size(); x++) {

                    if (cartItemModelList.get(x).getType() == CartItemModel.CART_ITEM && cartItemModelList.get(x).isInStock()) {
                        totalItems++;
                        totalItemPrice = totalItemPrice + Integer.parseInt(cartItemModelList.get(x).getProductPrice());

                    }

                }
                if (totalItemPrice > 500) {
                    deliveryPrice = "FREE";
                    totalAmount = totalItemPrice;
                } else {
                    deliveryPrice = "50";
                    totalAmount = totalItemPrice + 50;
                }
                ((CartTotalAmountViewholder) viewHolder).setTotalAmount(totalItems, totalItemPrice, deliveryPrice, totalAmount, savedAmount);
                break;
            default:
                return;
        }
        if (lastPosition < position) {
            Animation animation = AnimationUtils.loadAnimation(viewHolder.itemView.getContext(), R.anim.fade_in);
            viewHolder.itemView.setAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return cartItemModelList.size();
    }

    class CartItemViewholder extends RecyclerView.ViewHolder {

        private ImageView productImage;
        private ImageView freeCouponIcon;
        private TextView productTitle;
        private TextView freeCoupons;
        private TextView productPrice;
        private TextView cuttedPrice;
        private TextView offersApplied;
        private TextView couponsApplied;
        private TextView productQuantity;
        private LinearLayout couponRedemptionLayout;

        private LinearLayout deleteBtn;

        public CartItemViewholder(@NonNull View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.product_image);
            freeCouponIcon = itemView.findViewById(R.id.free_coupon_icon);
            productTitle = itemView.findViewById(R.id.product_title);
            freeCoupons = itemView.findViewById(R.id.tv_free_coupon);
            productPrice = itemView.findViewById(R.id.product_price);
            cuttedPrice = itemView.findViewById(R.id.cutted_price);
            offersApplied = itemView.findViewById(R.id.offers_applied);
            couponsApplied = itemView.findViewById(R.id.coupons_applied);
            productQuantity = itemView.findViewById(R.id.product_quantity);
            couponRedemptionLayout = itemView.findViewById(R.id.coupon_redemption_layout);

            deleteBtn = itemView.findViewById(R.id.remove_item_btn);
        }

        private void setItemDetails(String productID, String resource, String title, Long freeCouponsNo, String productPriceText, String cuttedPriceText, Long offersAppliedNo, int position, boolean inStock) {
            Glide.with(itemView.getContext()).load(resource).apply(new RequestOptions().placeholder(R.drawable.placeholder)).into(productImage);
            productTitle.setText(title);

            if (inStock) {
                if (freeCouponsNo > 0) {
                    freeCouponIcon.setVisibility(View.VISIBLE);
                    freeCoupons.setVisibility(View.VISIBLE);
                    if (freeCouponsNo == 1) {
                        freeCoupons.setText("Free " + freeCouponsNo + " Coupon");
                    } else {
                        freeCoupons.setText("Free " + freeCouponsNo + " Coupons");
                    }
                } else {
                    freeCouponIcon.setVisibility(View.INVISIBLE);
                    freeCoupons.setVisibility(View.INVISIBLE);
                }

                productPrice.setText(productPriceText + " MDL");
                productPrice.setTextColor(Color.parseColor("#000000"));
                cuttedPrice.setText(cuttedPriceText + " MDL");
                couponRedemptionLayout.setVisibility(View.VISIBLE);

                productQuantity.setOnClickListener(view -> {
                    Dialog quantityDialog = new Dialog(itemView.getContext());
                    quantityDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    quantityDialog.setContentView(R.layout.quantity_dialog);
                    quantityDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    quantityDialog.setCancelable(false);
                    EditText quantityNo = quantityDialog.findViewById(R.id.quantity_no);
                    Button cancelBtn = quantityDialog.findViewById(R.id.cancel_btn);
                    Button okBtn = quantityDialog.findViewById(R.id.ok_btn);

                    cancelBtn.setOnClickListener(view12 -> quantityDialog.dismiss());

                    okBtn.setOnClickListener(view1 -> {
                        if (quantityNo.getText() == null || quantityNo.getText().length() == 0) {
                            Toast.makeText(view1.getContext(), "Please insert correct value", Toast.LENGTH_SHORT).show();
                        } else {
                            try  {
                                int quantity = Integer.parseInt(quantityNo.getText().toString());
                                productQuantity.setText("Qty: " + quantityNo.getText());
                                int productPriceIntValue = Integer.parseInt(productPriceText);

                                productPrice.setText(quantity * productPriceIntValue + " MDL");
                                quantityDialog.dismiss();


                            } catch (Exception e) {
                                Toast.makeText(view1.getContext(), "Please insert correct value", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
                    quantityDialog.show();
                });
                if (offersAppliedNo > 0) {
                    offersApplied.setVisibility(View.VISIBLE);
                    offersApplied.setText(offersAppliedNo + " Offers applied");
                } else {
                    offersApplied.setVisibility(View.INVISIBLE);
                }
            } else {
                productPrice.setText("Out of stock");
                productPrice.setTextColor(itemView.getResources().getColor(R.color.purple_500));
                cuttedPrice.setText("");
                couponRedemptionLayout.setVisibility(View.GONE);
                freeCoupons.setVisibility(View.INVISIBLE);
                productQuantity.setVisibility(View.INVISIBLE);
                couponsApplied.setVisibility(View.INVISIBLE);
                offersApplied.setVisibility(View.INVISIBLE);
                freeCouponIcon.setVisibility(View.INVISIBLE);
            }

            if (showDeleteBtn) {
                deleteBtn.setVisibility(View.VISIBLE);
            } else {
                deleteBtn.setVisibility(View.GONE);
            }

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!ProductDetailsActivity.running_cart_query) {
                        ProductDetailsActivity.running_cart_query = true;
                        DBQueries.removeFromCart(position, itemView.getContext(),cartTotalAmount);
                    }
                }
            });

        }

    }

    class CartTotalAmountViewholder extends RecyclerView.ViewHolder {

        private TextView totalItems;
        private TextView totalItemPrice;
        private TextView deliveryPrice;
        private TextView totalAmount;
        private TextView savedAmount;

        public CartTotalAmountViewholder(@NonNull View itemView) {
            super(itemView);

            totalItems = itemView.findViewById(R.id.total_items);
            totalItemPrice = itemView.findViewById(R.id.total_items_price);
            deliveryPrice = itemView.findViewById(R.id.delivery_price);
            totalAmount = itemView.findViewById(R.id.total_price);
            savedAmount = itemView.findViewById(R.id.saved_amount);
        }

        private void setTotalAmount(int totalItemText, int totalItemPriceText, String deliveryPriceText, int totalAmountText, int savedAmountText) {
            totalItems.setText("Price(" + totalItemText + " items)");
            totalItemPrice.setText(totalItemPriceText + " MDL");
            if (deliveryPriceText.equals("FREE")) {
                deliveryPrice.setText(deliveryPriceText);
            } else {
                deliveryPrice.setText(deliveryPriceText + " MDL");
            }
            totalAmount.setText(totalAmountText + " MDL");
            cartTotalAmount.setText(totalAmountText + " MDL");
            savedAmount.setText("You saved " + savedAmountText + " MDL on this order");

            LinearLayout parent = (LinearLayout) cartTotalAmount.getParent().getParent();
            if (totalItemPriceText == 0) {
                DBQueries.cartItemModelList.remove(DBQueries.cartItemModelList.size() - 1);
                parent.setVisibility(View.GONE);
            } else {
                parent.setVisibility(View.VISIBLE);
            }
        }
    }
}
